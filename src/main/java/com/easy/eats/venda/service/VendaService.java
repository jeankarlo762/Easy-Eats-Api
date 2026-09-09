package com.easy.eats.venda.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.cashback.model.CashbackConfig;
import com.easy.eats.cashback.service.CashbackConfigService;
import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.cliente.repository.ClienteRepository;
import com.easy.eats.comanda.enums.StatusComanda;
import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.comanda.repository.ComandaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.service.ItemVendaService;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.usuario.repository.UsuarioRepository;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.quicksort.ProdutoRanking;
import com.easy.eats.venda.quicksort.QuickSortProdutos;
import com.easy.eats.venda.repository.VendaRepository;

@Service
public class VendaService {

    @Autowired
    VendaRepository repository;

    @Autowired
    ItemVendaService itemVendaService;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    MesaRepository mesaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ComandaRepository comandaRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    NotificacaoService notificacaoService;

    @Autowired
    CashbackConfigService cashbackConfigService;

    @Transactional
    public Venda criar(Venda venda) {
        Integer empresaId = SecurityUtils.getEmpresaId();

        boolean temMesa = venda.getMesa() != null && venda.getMesa().getId() != null;
        boolean temNomeCliente = venda.getNomeCliente() != null && !venda.getNomeCliente().isBlank();
        boolean temCliente = venda.getCliente() != null && venda.getCliente().getId() != null;

        if (!temMesa && !temNomeCliente && !temCliente) {
            throw new IllegalArgumentException("Informe a mesa ou o nome/cliente");
        }

        venda.setId(null);
        venda.setEmpresa(empresaRepository.getReferenceById(empresaId));
        venda.setMesa(temMesa ? mesaDaMesmaEmpresa(venda.getMesa(), empresaId) : null);
        venda.setUsuario(usuarioDaMesmaEmpresa(venda.getUsuario(), empresaId));
        venda.setComanda(comandaDaMesmaEmpresa(venda.getComanda(), empresaId));
        venda.setCliente(temCliente ? clienteDaMesmaEmpresa(venda.getCliente(), empresaId) : null);
        // Nada preenchia esta data — toda venda nascia sem registro de quando
        // foi criada, o que impossibilitaria qualquer relatório por período.
        venda.setDt_criacao(LocalDateTime.now().toString());

        Venda salva = repository.save(venda);

        String identificacao = salva.getMesa() != null
                ? "Mesa " + salva.getMesa().getNumero()
                : salva.getNomeCliente();
        notificacaoService.notificarEmpresa(empresaId, "bi-cart-check", "laranja", "Novo pedido recebido",
                identificacao + " — pedido #" + salva.getId() + " acabou de entrar na fila.");

        return salva;
    }

    public Venda salvar(Venda venda) {
        return repository.save(venda);
    }

    /**
     * Ao entregar a venda, credita cashback ao cliente cadastrado (se
     * houver) conforme a configuração da empresa — a única lógica de
     * acúmulo que existe hoje; sem isso o saldoCashback do Cliente nunca
     * saía de zero. Ainda não há débito automático no checkout do link
     * público (fica para quando esse fluxo existir).
     */
    @Transactional
    public Venda atualizarStatus(Venda vendaExistente, String novoStatus, String novoTipo) {
        boolean tornouEntregue = !"Entregue".equalsIgnoreCase(vendaExistente.getStatus())
                && "Entregue".equalsIgnoreCase(novoStatus);

        vendaExistente.setStatus(novoStatus);
        vendaExistente.setTipo(novoTipo);
        Venda salva = repository.save(vendaExistente);

        if (tornouEntregue) {
            acumularCashback(salva);
        }

        return salva;
    }

    private void acumularCashback(Venda venda) {
        if (venda.getCliente() == null) {
            return;
        }

        CashbackConfig config = cashbackConfigService.buscarOuCriar();
        if (config.getFlAtivo() == null || !config.getFlAtivo()) {
            return;
        }

        BigDecimal total = (venda.getItens() != null ? venda.getItens() : List.<ItemVenda>of()).stream()
                .map(ItemVenda::getValor_total)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (config.getValorMinimoParaAcumular() != null && total.compareTo(config.getValorMinimoParaAcumular()) < 0) {
            return;
        }

        BigDecimal credito = total.multiply(config.getPercentualAcumulo())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (credito.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Cliente cliente = clienteRepository.findById(venda.getCliente().getId())
                .orElseThrow(() -> new IllegalStateException("Cliente da venda não encontrado"));
        BigDecimal saldoAtual = cliente.getSaldoCashback() != null ? cliente.getSaldoCashback() : BigDecimal.ZERO;
        cliente.setSaldoCashback(saldoAtual.add(credito));
        clienteRepository.save(cliente);
    }

    public List<Venda> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    /** Histórico de compras do cliente — base para fidelidade e cashback. */
    public List<Venda> listarPorCliente(Integer clienteId) {
        Integer empresaId = SecurityUtils.getEmpresaId();
        if (clienteRepository.findByIdAndEmpresaId(clienteId, empresaId).isEmpty()) {
            throw new IllegalArgumentException("Cliente informado não existe ou não pertence à sua empresa");
        }
        return repository.findAllByCliente_IdAndEmpresaId(clienteId, empresaId);
    }

    public Optional<Venda> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }

    public List<ProdutoRanking> rankingProdutos() {
        List<ItemVenda> itens = itemVendaService.listarTodos();

        Map<Integer, ProdutoRanking> mapa = new HashMap<>();

        for (ItemVenda item : itens) {
            if (item.getProduto() == null) continue;

            Integer idProduto = item.getProduto().getId();
            String nome = item.getProduto().getNome();
            Double quantidade = item.getQuantidade() != null ? item.getQuantidade() : 0.0;
            BigDecimal valorTotal = item.getValor_total() != null ? item.getValor_total() : BigDecimal.ZERO;

            if (mapa.containsKey(idProduto)) {
                ProdutoRanking ranking = mapa.get(idProduto);
                ranking.setQuantidadeVendida(ranking.getQuantidadeVendida() + quantidade);
                ranking.setFaturamentoTotal(ranking.getFaturamentoTotal().add(valorTotal));
            } else {
                mapa.put(idProduto, new ProdutoRanking(nome, quantidade, valorTotal));
            }
        }

        List<ProdutoRanking> lista = new ArrayList<>(mapa.values());
        new QuickSortProdutos().ordenar(lista);
        return lista;
    }

    private Mesa mesaDaMesmaEmpresa(Mesa mesaRecebida, Integer empresaId) {
        if (mesaRecebida == null || mesaRecebida.getId() == null) {
            throw new IllegalArgumentException("A mesa é obrigatória");
        }
        return mesaRepository.findByIdAndEmpresaId(mesaRecebida.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Mesa informada não existe ou não pertence à sua empresa"));
    }

    private Cliente clienteDaMesmaEmpresa(Cliente clienteRecebido, Integer empresaId) {
        return clienteRepository.findByIdAndEmpresaId(clienteRecebido.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente informado não existe ou não pertence à sua empresa"));
    }

    private Usuario usuarioDaMesmaEmpresa(Usuario usuarioRecebido, Integer empresaId) {
        if (usuarioRecebido == null || usuarioRecebido.getId() == null) {
            throw new IllegalArgumentException("O usuário é obrigatório");
        }
        return usuarioRepository.findByIdAndEmpresaId(usuarioRecebido.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário informado não existe ou não pertence à sua empresa"));
    }

    /**
     * Comanda é opcional (só pedidos de mesa vinculados ao módulo de Comandas a
     * usam). Validada aqui, e não só no ComandaService, para que POST /venda
     * também rejeite uma comanda de outra empresa ou já fechada informada
     * diretamente por quem chamar a API sem passar pelo fluxo de comandas.
     */
    private Comanda comandaDaMesmaEmpresa(Comanda comandaRecebida, Integer empresaId) {
        if (comandaRecebida == null || comandaRecebida.getId() == null) {
            return null;
        }
        Comanda comanda = comandaRepository.findByIdAndEmpresaId(comandaRecebida.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Comanda informada não existe ou não pertence à sua empresa"));
        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new IllegalArgumentException("A comanda informada não está aberta");
        }
        return comanda;
    }
}
