package com.easy.eats.cardapiopublico.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.cardapiopublico.dto.CardapioPublicoResponse;
import com.easy.eats.cardapiopublico.dto.CardapioPublicoResponse.EmpresaPublica;
import com.easy.eats.cardapiopublico.dto.CardapioPublicoResponse.MesaPublica;
import com.easy.eats.cardapiopublico.dto.CardapioPublicoResponse.ProdutoPublico;
import com.easy.eats.cardapiopublico.dto.PedidoPublicoRequest;
import com.easy.eats.cardapiopublico.dto.PedidoPublicoResponse;
import com.easy.eats.comanda.enums.StatusComanda;
import com.easy.eats.comanda.repository.ComandaRepository;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.empresa.service.EmpresaService;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.repository.ItemVendaRepository;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.usuario.model.Role;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.repository.VendaRepository;

/**
 * Cardápio e pedido do cliente final via QR Code. Roda sem JWT, então nada
 * aqui pode usar SecurityUtils para descobrir a empresa: ela vem sempre do
 * slug da URL, e todo id recebido (mesa, produto) é conferido contra ela.
 */
@Service
public class CardapioPublicoService {

    private static final int MAX_ITENS_POR_PEDIDO = 50;
    private static final int MAX_QUANTIDADE_POR_ITEM = 99;
    private static final int MAX_TAMANHO_NOME = 80;
    private static final int MAX_TAMANHO_OBSERVACAO = 200;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    EmpresaService empresaService;

    @Autowired
    ProdutoRepository produtoRepository;

    @Autowired
    MesaRepository mesaRepository;

    @Autowired
    ComandaRepository comandaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    VendaRepository vendaRepository;

    @Autowired
    ItemVendaRepository itemVendaRepository;

    @Autowired
    NotificacaoService notificacaoService;

    /** Slug do link público da empresa do usuário logado, para montar o QR Code. */
    public String slugDaMinhaEmpresa() {
        Integer empresaId = SecurityUtils.getEmpresaId();
        if (empresaId == null) {
            throw new IllegalArgumentException("Usuário não está vinculado a nenhuma empresa");
        }
        return empresaService.garantirSlug(empresaService.buscarPorId(empresaId));
    }

    public CardapioPublicoResponse buscarCardapio(String slug, Integer mesaId) {
        Empresa empresa = empresaAtiva(slug);
        Mesa mesa = mesaId != null ? mesaDaEmpresa(mesaId, empresa.getId()) : null;

        List<ProdutoPublico> produtos = produtoRepository.findAllByEmpresaId(empresa.getId()).stream()
                .filter(this::disponivelNoCardapio)
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(p -> new ProdutoPublico(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(),
                        p.getCategoria() != null ? p.getCategoria().getNome() : null))
                .toList();

        return new CardapioPublicoResponse(
                new EmpresaPublica(empresa.getNome(), empresa.getSlug(), empresa.getHorarioFuncionamento()),
                mesa != null ? new MesaPublica(mesa.getId(), mesa.getNumero()) : null,
                produtos);
    }

    @Transactional
    public PedidoPublicoResponse criarPedido(String slug, PedidoPublicoRequest request) {
        Empresa empresa = empresaAtiva(slug);
        Integer empresaId = empresa.getId();

        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item ao pedido");
        }
        if (request.itens().size() > MAX_ITENS_POR_PEDIDO) {
            throw new IllegalArgumentException("O pedido pode ter no máximo " + MAX_ITENS_POR_PEDIDO + " itens");
        }

        String nomeCliente = textoLimpo(request.nomeCliente(), MAX_TAMANHO_NOME);
        Mesa mesa = request.mesaId() != null ? mesaDaEmpresa(request.mesaId(), empresaId) : null;
        if (mesa == null && nomeCliente == null) {
            throw new IllegalArgumentException("Informe seu nome para identificarmos o pedido");
        }

        Venda venda = new Venda();
        venda.setStatus("Aguardando");
        venda.setTipo(mesa != null ? "Mesa" : "Balcão");
        venda.setOrigem("QRCODE");
        venda.setMesa(mesa);
        venda.setNomeCliente(nomeCliente);
        venda.setEmpresa(empresa);
        venda.setUsuario(usuarioResponsavel(empresaId));
        venda.setDt_criacao(LocalDateTime.now().toString());
        if (mesa != null) {
            // Mesma regra do Novo Pedido: com comanda aberta na mesa, o pedido
            // entra nela para ser cobrado junto no fechamento.
            comandaRepository.findAllByEmpresaIdAndStatus(empresaId, StatusComanda.ABERTA).stream()
                    .filter(c -> c.getMesa() != null && Objects.equals(c.getMesa().getId(), mesa.getId()))
                    .findFirst()
                    .ifPresent(venda::setComanda);
        }

        List<ItemVenda> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PedidoPublicoRequest.Item recebido : request.itens()) {
            ItemVenda item = montarItem(recebido, empresaId, venda);
            itens.add(item);
            total = total.add(item.getValor_total());
        }

        Venda salva = vendaRepository.save(venda);
        itemVendaRepository.saveAll(itens);

        String identificacao = mesa != null ? "Mesa " + mesa.getNumero() : nomeCliente;
        notificacaoService.notificarEmpresa(empresaId, "bi-qr-code", "laranja", "Novo pedido pelo QR Code",
                identificacao + " — pedido #" + salva.getId() + " acabou de entrar na fila.");

        return new PedidoPublicoResponse(salva.getId(), salva.getStatus(), total);
    }

    private ItemVenda montarItem(PedidoPublicoRequest.Item recebido, Integer empresaId, Venda venda) {
        if (recebido == null || recebido.produtoId() == null) {
            throw new IllegalArgumentException("O produto é obrigatório");
        }
        int quantidade = recebido.quantidade() != null ? recebido.quantidade() : 1;
        if (quantidade < 1 || quantidade > MAX_QUANTIDADE_POR_ITEM) {
            throw new IllegalArgumentException("Quantidade inválida");
        }

        Produto produto = produtoRepository.findByIdAndEmpresaId(recebido.produtoId(), empresaId)
                .filter(this::disponivelNoCardapio)
                .orElseThrow(() -> new IllegalArgumentException("Um dos produtos não está mais disponível"));

        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setProduto(produto);
        item.setQuantidade((double) quantidade);
        item.setPreco_unitario(produto.getPreco());
        item.setCusto_unitario(produto.getCusto());
        item.setValor_total(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
        item.setObservacao(textoLimpo(recebido.observacao(), MAX_TAMANHO_OBSERVACAO));
        return item;
    }

    /** Insumo não é vendido ao cliente, e produto sem preço quebraria o total. */
    private boolean disponivelNoCardapio(Produto produto) {
        return !Boolean.FALSE.equals(produto.getFlAtivo())
                && produto.getNatureza() != NaturezaProduto.INSUMO
                && produto.getPreco() != null
                && produto.getPreco().compareTo(BigDecimal.ZERO) > 0;
    }

    private Empresa empresaAtiva(String slug) {
        return empresaRepository.findBySlug(slug)
                .filter(e -> !Boolean.FALSE.equals(e.getFlAtivo()))
                .orElseThrow(() -> new IllegalArgumentException("Cardápio não encontrado"));
    }

    private Mesa mesaDaEmpresa(Integer mesaId, Integer empresaId) {
        return mesaRepository.findByIdAndEmpresaId(mesaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Mesa não encontrada"));
    }

    /**
     * Venda exige um Usuario (coluna NOT NULL), mas no pedido pelo QR ninguém
     * está logado. Atribui ao administrador da empresa; a origem QRCODE é o
     * que identifica de fato que o pedido veio do cliente.
     */
    private Usuario usuarioResponsavel(Integer empresaId) {
        List<Usuario> ativos = usuarioRepository.findAllByEmpresaId(empresaId).stream()
                .filter(u -> !Boolean.FALSE.equals(u.getFlAtivo()))
                .toList();
        return ativos.stream()
                .filter(u -> u.getRole() == Role.ADMINISTRADOR)
                .findFirst()
                .or(() -> ativos.stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("Estabelecimento sem usuário para receber pedidos"));
    }

    private String textoLimpo(String texto, int tamanhoMaximo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.length() > tamanhoMaximo ? limpo.substring(0, tamanhoMaximo) : limpo;
    }
}
