package com.easyeats.api.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easyeats.api.dto.CardapioPublicoDto;
import com.easyeats.api.dto.ItemPedidoDto;
import com.easyeats.api.dto.PedidoCardapioCriadoDto;
import com.easyeats.api.dto.PedidoCardapioDto;
import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.ItemPedido;
import com.easyeats.api.entity.Pedido;
import com.easyeats.api.entity.Preco;
import com.easyeats.api.entity.Produto;
import com.easyeats.api.entity.StatusPedidoEnum;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;
import com.easyeats.api.exception.RegraNegocioException;
import com.easyeats.api.integracao.whatsapp.NotificacaoPedidoWhatsApp;
import com.easyeats.api.repository.PedidoRepository;
import com.easyeats.api.repository.PrecoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Cardápio digital aberto pelo QR Code: lista pública dos produtos e pedido feito pelo próprio cliente
 * (delivery ou retirada). O pedido entra como comanda ABERTA de uma venda nova, igual a um pedido lançado no
 * sistema, e aparece na cozinha e na tela de mesas/retiradas. Ao confirmar, o WhatsApp é avisado.
 */
@Service
@RequiredArgsConstructor
public class CardapioDigitalService {

    private static final List<FormaPagamentoEnum> FORMAS_ACEITAS = List.of(
            FormaPagamentoEnum.PIX, FormaPagamentoEnum.DINHEIRO, FormaPagamentoEnum.CREDITO,
            FormaPagamentoEnum.DEBITO);

    private final PrecoRepository precoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoService itemPedidoService;
    private final EstoqueService estoqueService;
    private final VendaService vendaService;
    private final NotificacaoPedidoWhatsApp notificacao;

    @Value("${cardapio.nome-estabelecimento:Easy Eats}")
    private String nomeEstabelecimento;

    @Value("${cardapio.aceita-delivery:true}")
    private boolean aceitaDelivery;

    @Value("${cardapio.aceita-retirada:true}")
    private boolean aceitaRetirada;

    @Transactional(readOnly = true)
    public CardapioPublicoDto cardapio() {
        Map<Long, CardapioPublicoDto.Categoria> categorias = new LinkedHashMap<>();
        Map<Long, Boolean> jaListado = new LinkedHashMap<>();
        for (Preco preco : precoRepository.buscarPrecosAtivosDoCardapio()) {
            Produto produto = preco.getProduto();
            // ordenado por id desc: o primeiro preço de cada produto é o ativo mais recente
            if (jaListado.putIfAbsent(produto.getId(), true) != null) {
                continue;
            }
            CardapioPublicoDto.Categoria categoria = categorias.computeIfAbsent(produto.getCategoria().getId(),
                    id -> new CardapioPublicoDto.Categoria(id, produto.getCategoria().getNome(), new ArrayList<>()));
            categoria.produtos().add(new CardapioPublicoDto.Produto(
                    produto.getId(), produto.getNome(), produto.getDescricao(), preco.getValor()));
        }
        return new CardapioPublicoDto(nomeEstabelecimento, aceitaDelivery, aceitaRetirada, FORMAS_ACEITAS,
                List.copyOf(categorias.values()));
    }

    @Transactional
    public PedidoCardapioCriadoDto fazerPedido(PedidoCardapioDto dto) {
        TipoVendaEnum tipo = dto.tipoEntrega() == null ? TipoVendaEnum.DELIVERY : dto.tipoEntrega();
        if ((tipo == TipoVendaEnum.DELIVERY && !aceitaDelivery) || (tipo == TipoVendaEnum.RETIRADA && !aceitaRetirada)
                || tipo == TipoVendaEnum.MESA) {
            throw new RegraNegocioException("Tipo de entrega indisponível no momento");
        }
        if (!FORMAS_ACEITAS.contains(dto.formaPagamento())) {
            throw new RegraNegocioException("Forma de pagamento indisponível");
        }
        String telefone = dto.telefone().replaceAll("\\D", "");
        if (telefone.length() < 10 || telefone.length() > 13) {
            throw new RegraNegocioException("Informe um telefone válido com DDD");
        }

        List<ItemPedido> itens = dto.itens().stream()
                .map(item -> itemPedidoService.montarItem(
                        new ItemPedidoDto(null, null, item.produtoId(), item.quantidade(), null)))
                .toList();
        estoqueService.validarDisponibilidade(itens);

        BigDecimal total = itens.stream()
                .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (dto.formaPagamento() == FormaPagamentoEnum.DINHEIRO && dto.trocoPara() != null
                && dto.trocoPara().compareTo(total) < 0) {
            throw new RegraNegocioException("O troco deve ser para um valor maior ou igual ao total do pedido");
        }

        String endereco = tipo == TipoVendaEnum.DELIVERY ? formatarEndereco(dto.endereco()) : null;
        Venda venda = vendaService.abrirPeloCardapio(tipo, dto.nome(), telefone, endereco,
                dto.formaPagamento(), dto.trocoPara(), dto.observacao());

        Pedido pedido = new Pedido();
        pedido.setVenda(venda);
        pedido.setCliente(venda.getNomeCliente());
        pedido.setObservacao(dto.observacao() == null || dto.observacao().isBlank() ? null : dto.observacao().trim());
        pedido.setStatus(StatusPedidoEnum.ABERTO);
        pedido.setValorTotal(total);
        itens.forEach(pedido::adicionarItem);
        pedido = pedidoRepository.save(pedido);

        notificacao.novoPedido(venda, pedido, nomeEstabelecimento);
        return new PedidoCardapioCriadoDto(pedido.getId(), tipo, total, dto.formaPagamento());
    }

    /** Endereço em uma linha. Exige rua, número e bairro. */
    private static String formatarEndereco(PedidoCardapioDto.Endereco e) {
        if (e == null || vazio(e.rua()) || vazio(e.numero()) || vazio(e.bairro())) {
            throw new RegraNegocioException("Informe rua, número e bairro para a entrega");
        }
        StringJoiner linha = new StringJoiner(", ");
        linha.add(e.rua().trim() + ", " + e.numero().trim());
        if (!vazio(e.complemento())) linha.add(e.complemento().trim());
        linha.add(e.bairro().trim());
        if (!vazio(e.cidade())) linha.add(e.cidade().trim());
        if (!vazio(e.cep())) linha.add("CEP " + e.cep().replaceAll("\\D", ""));
        String texto = linha.toString();
        if (!vazio(e.referencia())) texto += " (Ref.: " + e.referencia().trim() + ")";
        return texto;
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}
