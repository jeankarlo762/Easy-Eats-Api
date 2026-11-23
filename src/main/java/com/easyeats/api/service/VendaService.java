package com.easyeats.api.service;

import com.easyeats.api.dto.AberturaVendaDto;
import com.easyeats.api.dto.AtualizacaoVendaDto;
import com.easyeats.api.dto.CancelamentoVendaDto;
import com.easyeats.api.dto.FiltroVendaDto;
import com.easyeats.api.dto.OcupacaoMesaDto;
import com.easyeats.api.dto.PagamentoVendaDto;
import com.easyeats.api.dto.PaginaDto;
import com.easyeats.api.dto.ResumoVendasDto;
import com.easyeats.api.dto.TotalPeriodoDto;
import com.easyeats.api.dto.TotalVendaDto;
import com.easyeats.api.dto.VendaDto;
import com.easyeats.api.dto.VendaResumoDto;
import com.easyeats.api.dto.mapper.VendaMapper;
import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.Mesa;
import com.easyeats.api.entity.PagamentoVenda;
import com.easyeats.api.entity.Pedido;
import com.easyeats.api.entity.StatusPedidoEnum;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;
import com.easyeats.api.exception.RecursoNaoEncontradoException;
import com.easyeats.api.exception.RegraNegocioException;
import com.easyeats.api.repository.MesaRepository;
import com.easyeats.api.repository.PagamentoVendaRepository;
import com.easyeats.api.repository.PedidoRepository;
import com.easyeats.api.repository.UsuarioRepository;
import com.easyeats.api.repository.VendaRepository;
import com.easyeats.api.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final VendaRepository vendaRepository;
    private final PagamentoVendaRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaixaService caixaService;
    private final EstoqueService estoqueService;
    private final VendaMapper mapper;

    // ------------------------------------------------------------------ abertura

    /** Abertura explícita. Para mesas a venda abre sozinha na primeira comanda. */
    @Transactional
    public VendaDto abrir(AberturaVendaDto dto, UsuarioAutenticado usuario) {
        TipoVendaEnum tipo = dto.tipo() != null
                ? dto.tipo()
                : (dto.mesaId() != null ? TipoVendaEnum.MESA : TipoVendaEnum.RETIRADA);
        exigirTipoDisponivel(tipo);
        String nomeCliente = textoOuNull(dto.nomeCliente());

        Mesa mesa = null;
        if (tipo == TipoVendaEnum.MESA) {
            if (dto.mesaId() == null) {
                throw new RegraNegocioException("Informe a mesa da venda");
            }
            mesa = buscarMesaParaAtualizar(dto.mesaId());
            if (vendaRepository.existsByMesaIdAndStatus(mesa.getId(), StatusVendaEnum.ABERTA)) {
                throw new RegraNegocioException("A mesa " + mesa.getNumero() + " já possui uma venda aberta");
            }
        } else {
            if (dto.mesaId() != null) {
                throw new RegraNegocioException("A retirada não usa mesa");
            }
            exigirNomeDaRetirada(nomeCliente);
        }

        Venda venda = vendaRepository.save(
                novaVenda(tipo, mesa, nomeCliente, textoOuNull(dto.observacao()), usuario));
        return paraDto(venda);
    }

    /**
     * Venda aberta da mesa, criada se ainda não existir. A linha da mesa é bloqueada, então duas comandas
     * simultâneas na mesma mesa não criam duas vendas.
     */
    @Transactional
    public Venda obterAbertaOuAbrir(Long mesaId, String nomeCliente, UsuarioAutenticado usuario) {
        Mesa mesa = buscarMesaParaAtualizar(mesaId);
        return vendaRepository.findByMesaIdAndStatus(mesaId, StatusVendaEnum.ABERTA)
                .orElseGet(() -> vendaRepository.save(
                        novaVenda(TipoVendaEnum.MESA, mesa, textoOuNull(nomeCliente), null, usuario)));
    }

    /**
     * Venda em que uma nova comanda será lançada, conforme o tipo de atendimento. Usada pelo PedidoService.
     * Mesa: venda aberta da mesa. Retirada: nova venda (ou a retirada já aberta informada em {@code vendaId}).
     */
    @Transactional
    public Venda vendaParaNovaComanda(TipoVendaEnum tipoInformado, Long mesaId, Long vendaId, String cliente,
            UsuarioAutenticado usuario) {
        TipoVendaEnum tipo = tipoInformado == null ? TipoVendaEnum.MESA : tipoInformado;
        exigirTipoDisponivel(tipo);
        if (tipo == TipoVendaEnum.MESA) {
            return obterAbertaOuAbrir(mesaId, cliente, usuario);
        }

        if (vendaId != null) {
            Venda existente = exigirAberta(buscarParaAtualizar(vendaId), "A venda não está aberta");
            if (existente.getTipo() != TipoVendaEnum.RETIRADA) {
                throw new RegraNegocioException("A venda informada não é uma retirada");
            }
            return existente;
        }
        String nomeCliente = textoOuNull(cliente);
        exigirNomeDaRetirada(nomeCliente);
        return vendaRepository.save(novaVenda(TipoVendaEnum.RETIRADA, null, nomeCliente, null, usuario));
    }

    // ------------------------------------------------------------------ regras usadas pelas comandas

    /** Comandas só podem ser incluídas, alteradas ou excluídas enquanto a venda está aberta. */
    public void exigirEditavel(Venda venda) {
        if (venda == null || venda.getStatus() == StatusVendaEnum.ABERTA) {
            return;
        }
        String situacao = venda.getStatus() == StatusVendaEnum.FINALIZADA ? "finalizada" : "cancelada";
        throw new RegraNegocioException("A venda desta comanda já foi " + situacao
                + "; não é possível alterar a comanda");
    }

    /**
     * O status de preparo continua livre depois do pagamento (a cozinha ainda pode estar terminando),
     * mas não muda em venda cancelada e uma comanda de venda finalizada não pode ser cancelada.
     */
    public void exigirTransicaoPermitida(Venda venda, StatusPedidoEnum novoStatus) {
        if (venda == null) {
            return;
        }
        if (venda.getStatus() == StatusVendaEnum.CANCELADA) {
            throw new RegraNegocioException("A venda desta comanda foi cancelada");
        }
        if (venda.getStatus() == StatusVendaEnum.FINALIZADA && novoStatus == StatusPedidoEnum.CANCELADO) {
            throw new RegraNegocioException(
                    "A venda desta comanda já foi finalizada; a comanda não pode ser cancelada");
        }
    }

    /** A comanda não troca de mesa: a venda pertence a uma mesa (troca e junção de mesa ficam para depois). */
    public void exigirMesaDaVenda(Venda venda, Long mesaId) {
        if (venda != null && venda.getMesa() != null && !venda.getMesa().getId().equals(mesaId)) {
            throw new RegraNegocioException(
                    "Não é possível mudar a comanda para outra mesa depois de lançada");
        }
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public VendaDto buscarPorId(Long id) {
        return paraDto(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Optional<VendaDto> buscarAbertaDaMesa(Long mesaId) {
        return vendaRepository.findByMesaIdAndStatus(mesaId, StatusVendaEnum.ABERTA).map(this::paraDto);
    }

    /** Todas as vendas abertas (mesas e retiradas), da mais antiga para a mais nova. Duas consultas. */
    @Transactional(readOnly = true)
    public List<VendaResumoDto> listarAbertas() {
        Map<Long, TotalVendaDto> totais = totaisDasVendasAbertas();
        return vendaRepository.findByStatusOrderByDataAberturaAscIdAsc(StatusVendaEnum.ABERTA).stream()
                .map(venda -> {
                    TotalVendaDto total = totais.get(venda.getId());
                    return mapper.toResumo(venda, total == null ? ZERO : total.total().setScale(2));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaDto<VendaResumoDto> listar(FiltroVendaDto filtro, int pagina, int tamanho,
            UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.DESC, "dataAbertura", "id"));
        return PaginaDto.de(vendaRepository.findAll(VendaRepository.comFiltros(filtro), pageable)
                .map(venda -> mapper.toResumo(venda, totalDe(venda))));
    }

    /** Faturamento do período (inclusivo) pela data de fechamento das vendas. Sem datas, considera hoje. */
    @Transactional(readOnly = true)
    public ResumoVendasDto resumir(LocalDate inicio, LocalDate fim, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        LocalDate de = inicio != null ? inicio : LocalDate.now();
        LocalDate ate = fim != null ? fim : de;
        LocalDateTime deHora = de.atStartOfDay();
        LocalDateTime ateHora = ate.plusDays(1).atStartOfDay();

        TotalPeriodoDto finalizadas = vendaRepository.totalizarPeriodo(StatusVendaEnum.FINALIZADA, deHora, ateHora);
        TotalPeriodoDto canceladas = vendaRepository.totalizarPeriodo(StatusVendaEnum.CANCELADA, deHora, ateHora);

        long quantidade = finalizadas.quantidade();
        BigDecimal total = finalizadas.total() == null ? ZERO : finalizadas.total().setScale(2);
        BigDecimal ticketMedio = quantidade == 0
                ? ZERO
                : total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);

        return new ResumoVendasDto(
                total,
                quantidade,
                ticketMedio,
                canceladas.quantidade(),
                pagamentoRepository.totalizarPorForma(StatusVendaEnum.FINALIZADA, deHora, ateHora),
                vendaRepository.totalizarPorDia(StatusVendaEnum.FINALIZADA, deHora, ateHora));
    }

    /** Situação de todas as mesas ativas: ocupada = existe venda aberta. Três consultas, sem N+1. */
    @Transactional(readOnly = true)
    public List<OcupacaoMesaDto> ocupacaoDasMesas() {
        Map<Long, Venda> abertasPorMesa = vendaRepository.findByStatus(StatusVendaEnum.ABERTA).stream()
                .filter(venda -> venda.getMesa() != null)
                .collect(Collectors.toMap(venda -> venda.getMesa().getId(), Function.identity()));
        Map<Long, TotalVendaDto> totais = totaisDasVendasAbertas();

        return mesaRepository.findByFlativo("S").stream()
                .sorted(Comparator.comparing(Mesa::getNumero))
                .map(mesa -> {
                    Venda venda = abertasPorMesa.get(mesa.getId());
                    if (venda == null) {
                        return new OcupacaoMesaDto(mesa.getId(), mesa.getNumero(), false, null, null, 0L, ZERO);
                    }
                    TotalVendaDto total = totais.get(venda.getId());
                    return new OcupacaoMesaDto(mesa.getId(), mesa.getNumero(), true, venda.getId(),
                            venda.getNomeCliente(),
                            total == null ? 0L : total.quantidadeComandas(),
                            total == null ? ZERO : total.total().setScale(2));
                })
                .toList();
    }

    // ------------------------------------------------------------------ edição e pagamentos

    @Transactional
    public VendaDto atualizar(Long id, AtualizacaoVendaDto dto) {
        Venda venda = exigirAberta(buscarParaAtualizar(id), "A venda não está aberta");
        venda.setNomeCliente(textoOuNull(dto.nomeCliente()));
        venda.setObservacao(textoOuNull(dto.observacao()));
        return paraDto(vendaRepository.save(venda));
    }

    /**
     * Pode ser registrado a qualquer momento enquanto a venda está aberta, inclusive antes de a cozinha
     * terminar. O total pago nunca passa do total atual da venda.
     */
    @Transactional
    public VendaDto registrarPagamento(Long id, PagamentoVendaDto dto, UsuarioAutenticado usuario) {
        Venda venda = exigirAberta(buscarParaAtualizar(id), "A venda não está aberta");
        BigDecimal valor = dto.valor().setScale(2);
        BigDecimal restante = calcularTotal(id).subtract(somar(pagamentoRepository.findByVendaIdOrderByIdAsc(id)));

        if (valor.compareTo(restante) > 0) {
            throw new RegraNegocioException("O pagamento de R$ " + valor.toPlainString()
                    + " ultrapassa o valor restante da venda (R$ " + restante.max(ZERO).toPlainString() + ")");
        }

        BigDecimal recebido = null;
        if (dto.forma() == FormaPagamentoEnum.DINHEIRO) {
            recebido = dto.valorRecebido() == null ? valor : dto.valorRecebido().setScale(2);
            if (recebido.compareTo(valor) < 0) {
                throw new RegraNegocioException(
                        "O valor recebido em dinheiro não pode ser menor que o valor do pagamento");
            }
        }

        PagamentoVenda pagamento = new PagamentoVenda();
        pagamento.setVenda(venda);
        pagamento.setForma(dto.forma());
        pagamento.setValor(valor);
        pagamento.setValorRecebido(recebido);
        pagamento.setUsuario(usuario.username());
        pagamentoRepository.save(pagamento);
        return paraDto(venda);
    }

    /** Nenhum dinheiro entra no caixa antes de finalizar, então remover um pagamento não exige estorno. */
    @Transactional
    public VendaDto removerPagamento(Long id, Long pagamentoId) {
        Venda venda = exigirAberta(buscarParaAtualizar(id), "A venda não está aberta");
        PagamentoVenda pagamento = pagamentoRepository.findById(pagamentoId)
                .filter(p -> p.getVenda().getId().equals(id))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento", pagamentoId));
        pagamentoRepository.delete(pagamento);
        pagamentoRepository.flush();
        return paraDto(venda);
    }

    // ------------------------------------------------------------------ finalização e cancelamento

    /**
     * Exige pagamentos que somem exatamente o total e um caixa aberto. Libera a mesa e lança no caixa o
     * dinheiro recebido (a parte paga em dinheiro, já descontado o troco).
     */
    @Transactional
    public VendaDto finalizar(Long id, UsuarioAutenticado usuario) {
        Venda venda = exigirAberta(buscarParaAtualizar(id), "A venda não está aberta");

        if (pedidoRepository.countByVendaIdAndStatusNot(id, StatusPedidoEnum.CANCELADO) == 0) {
            throw new RegraNegocioException("A venda não tem comandas para finalizar");
        }

        BigDecimal total = calcularTotal(id);
        List<PagamentoVenda> pagamentos = pagamentoRepository.findByVendaIdOrderByIdAsc(id);
        BigDecimal pago = somar(pagamentos);
        if (pago.compareTo(total) != 0) {
            throw new RegraNegocioException("O valor pago (R$ " + pago.toPlainString()
                    + ") difere do total da venda (R$ " + total.toPlainString() + ")");
        }

        BigDecimal emDinheiro = somar(pagamentos.stream()
                .filter(p -> p.getForma() == FormaPagamentoEnum.DINHEIRO)
                .toList());
        Caixa caixa = caixaService.registrarVenda(id, emDinheiro, usuario);

        venda.setStatus(StatusVendaEnum.FINALIZADA);
        venda.setMesaAbertaId(null);
        venda.setValorTotal(total);
        venda.setCaixa(caixa);
        venda.setUsuarioFechamento(usuario.username());
        venda.setDataFechamento(LocalDateTime.now());
        return paraDto(vendaRepository.save(venda));
    }

    /**
     * Cancela a venda e todas as comandas dela (Administrador e Gerente). Com {@code devolverEstoque} (e a
     * senha do gerente), os ingredientes voltam ao estoque nas comandas que baixaram o estoque mas ainda
     * não foram usadas: ABERTO (voltou ao início) ou EM_PREPARO. Comandas prontas nunca devolvem estoque.
     */
    @Transactional
    public VendaDto cancelar(Long id, CancelamentoVendaDto dto, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        boolean devolverEstoque = Boolean.TRUE.equals(dto.devolverEstoque());
        if (devolverEstoque) {
            confirmarSenhaDoGerente(usuario, dto.senhaGerente());
        }
        Venda venda = exigirAberta(buscarParaAtualizar(id), "Só é possível cancelar uma venda aberta");

        List<Pedido> comandas = pedidoRepository.findByVendaIdOrderByDataCriacaoAscIdAsc(id);
        for (Pedido comanda : comandas) {
            if (comanda.getStatus() == StatusPedidoEnum.CANCELADO) {
                continue;
            }
            boolean naoUsada = comanda.getStatus() == StatusPedidoEnum.ABERTO
                    || comanda.getStatus() == StatusPedidoEnum.EM_PREPARO;
            if (devolverEstoque && naoUsada && comanda.isEstoqueBaixado()) {
                estoqueService.devolverPorItens(comanda.getItens());
                comanda.setEstoqueBaixado(false);
            }
            comanda.setStatus(StatusPedidoEnum.CANCELADO);
        }
        pedidoRepository.saveAll(comandas);

        venda.setStatus(StatusVendaEnum.CANCELADA);
        venda.setMesaAbertaId(null);
        venda.setMotivoCancelamento(dto.motivo().trim());
        venda.setUsuarioFechamento(usuario.username());
        venda.setDataFechamento(LocalDateTime.now());
        return paraDto(vendaRepository.save(venda));
    }

    // ------------------------------------------------------------------ apoio

    private Venda novaVenda(TipoVendaEnum tipo, Mesa mesa, String nomeCliente, String observacao,
            UsuarioAutenticado usuario) {
        Venda venda = new Venda();
        venda.setTipo(tipo);
        venda.setMesa(mesa);
        venda.setMesaAbertaId(mesa == null ? null : mesa.getId());
        venda.setStatus(StatusVendaEnum.ABERTA);
        venda.setNomeCliente(nomeCliente);
        venda.setObservacao(observacao);
        venda.setUsuarioAbertura(usuario.username());
        return venda;
    }

    private void exigirTipoDisponivel(TipoVendaEnum tipo) {
        if (tipo == TipoVendaEnum.DELIVERY) {
            throw new RegraNegocioException("O delivery estará disponível em breve");
        }
    }

    // O cliente é chamado pelo nome quando o pedido fica pronto para retirar
    private void exigirNomeDaRetirada(String nomeCliente) {
        if (nomeCliente == null) {
            throw new RegraNegocioException("Informe o nome do cliente da retirada");
        }
    }

    private Mesa buscarMesaParaAtualizar(Long mesaId) {
        return mesaRepository.buscarPorIdParaAtualizar(mesaId)
                .filter(mesa -> "S".equals(mesa.getFlativo()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mesa", mesaId));
    }

    private Venda buscarEntidade(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    private Venda buscarParaAtualizar(Long id) {
        return vendaRepository.buscarPorIdParaAtualizar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    private Venda exigirAberta(Venda venda, String mensagem) {
        if (venda.getStatus() != StatusVendaEnum.ABERTA) {
            throw new RegraNegocioException(mensagem);
        }
        return venda;
    }

    private void exigirGestao(UsuarioAutenticado usuario) {
        if (!usuario.gestao()) {
            throw new AccessDeniedException("Operação permitida apenas para Administrador ou Gerente");
        }
    }

    // O gerente confirma a devolução de estoque digitando a própria senha
    private void confirmarSenhaDoGerente(UsuarioAutenticado usuario, String senha) {
        if (senha == null || senha.isBlank()) {
            throw new RegraNegocioException("Informe a senha do gerente para devolver o estoque");
        }
        boolean confere = usuarioRepository.findByUsername(usuario.username())
                .map(u -> passwordEncoder.matches(senha, u.getPassword()))
                .orElse(false);
        if (!confere) {
            throw new RegraNegocioException("Senha do gerente incorreta");
        }
    }

    private Map<Long, TotalVendaDto> totaisDasVendasAbertas() {
        return pedidoRepository.totaisDasVendasAbertas(StatusVendaEnum.ABERTA, StatusPedidoEnum.CANCELADO).stream()
                .collect(Collectors.toMap(TotalVendaDto::vendaId, Function.identity()));
    }

    private VendaDto paraDto(Venda venda) {
        List<Pedido> comandas = pedidoRepository.findByVendaIdOrderByDataCriacaoAscIdAsc(venda.getId());
        List<PagamentoVenda> pagamentos = pagamentoRepository.findByVendaIdOrderByIdAsc(venda.getId());
        return mapper.toDto(venda, totalDe(venda), comandas, pagamentos);
    }

    // Finalizada: valor gravado no fechamento. Aberta: calculado. Cancelada: zero (comandas canceladas).
    private BigDecimal totalDe(Venda venda) {
        if (venda.getStatus() == StatusVendaEnum.FINALIZADA && venda.getValorTotal() != null) {
            return venda.getValorTotal();
        }
        return calcularTotal(venda.getId());
    }

    // Soma dos itens das comandas não canceladas (independe do valorTotal gravado em cada Pedido)
    private BigDecimal calcularTotal(Long vendaId) {
        BigDecimal total = pedidoRepository.somarValorDaVenda(vendaId, StatusPedidoEnum.CANCELADO);
        return total == null ? ZERO : total.setScale(2);
    }

    private static BigDecimal somar(List<PagamentoVenda> pagamentos) {
        return pagamentos.stream()
                .map(PagamentoVenda::getValor)
                .reduce(ZERO, BigDecimal::add);
    }

    private static String textoOuNull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
