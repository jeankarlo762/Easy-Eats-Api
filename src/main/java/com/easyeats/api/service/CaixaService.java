package com.easyeats.api.service;

import com.easyeats.api.dto.AberturaCaixaDto;
import com.easyeats.api.dto.CaixaDto;
import com.easyeats.api.dto.FechamentoCaixaDto;
import com.easyeats.api.dto.FiltroLancamentosDto;
import com.easyeats.api.dto.MovimentacaoCaixaDto;
import com.easyeats.api.dto.PaginaDto;
import com.easyeats.api.dto.RelatorioFechamentoDto;
import com.easyeats.api.dto.ResumoLancamentosDto;
import com.easyeats.api.dto.TotalPorTipoDto;
import com.easyeats.api.dto.mapper.CaixaMapper;
import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.MovimentacaoCaixa;
import com.easyeats.api.entity.StatusCaixaEnum;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.exception.RecursoNaoEncontradoException;
import com.easyeats.api.exception.RegraNegocioException;
import com.easyeats.api.repository.CaixaRepository;
import com.easyeats.api.repository.MovimentacaoCaixaRepository;
import com.easyeats.api.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaixaService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final CaixaRepository caixaRepository;
    private final MovimentacaoCaixaRepository movimentacaoRepository;
    private final CaixaMapper mapper;

    // ------------------------------------------------------------------ abertura

    @Transactional
    public CaixaDto abrir(AberturaCaixaDto dto, UsuarioAutenticado usuario) {
        if (caixaRepository.existsByStatus(StatusCaixaEnum.ABERTO)) {
            throw new RegraNegocioException("Já existe um caixa aberto");
        }

        Caixa caixa = new Caixa();
        caixa.setStatus(StatusCaixaEnum.ABERTO);
        caixa.setAbertoUnico(true);
        caixa.setFundoCaixa(dto.fundoCaixa().setScale(2));
        caixa.setUsuarioAbertura(usuario.username());
        caixa.setObservacaoAbertura(textoOuNull(dto.observacao()));

        try {
            caixa = caixaRepository.saveAndFlush(caixa);
        } catch (DataIntegrityViolationException e) {
            // duas aberturas simultâneas: a restrição unique de abertoUnico barra a segunda
            throw new RegraNegocioException("Já existe um caixa aberto");
        }

        if (caixa.getFundoCaixa().signum() > 0) {
            movimentacaoRepository.save(novaMovimentacao(caixa, TipoMovimentacaoCaixaEnum.FUNDO_INICIAL,
                    caixa.getFundoCaixa(), "Fundo de caixa", usuario));
        }
        return mapper.toDto(caixa, caixa.getFundoCaixa());
    }

    // ------------------------------------------------------------------ movimentações

    @Transactional
    public MovimentacaoCaixaDto registrarMovimentacao(Long caixaId, MovimentacaoCaixaDto dto,
            UsuarioAutenticado usuario) {
        TipoMovimentacaoCaixaEnum tipo = dto.tipo();
        if (tipo == TipoMovimentacaoCaixaEnum.FUNDO_INICIAL) {
            throw new RegraNegocioException("O fundo de caixa só pode ser informado na abertura");
        }
        if (tipo == TipoMovimentacaoCaixaEnum.VENDA) {
            throw new RegraNegocioException("As vendas entram no caixa ao finalizar a venda");
        }        if (tipo == TipoMovimentacaoCaixaEnum.SANGRIA) {
            exigirGestao(usuario);
            if (dto.descricao() == null || dto.descricao().isBlank()) {
                throw new RegraNegocioException("Informe o motivo da sangria");
            }
        }

        Caixa caixa = buscarAbertoParaAtualizar(caixaId);
        BigDecimal valor = dto.valor().setScale(2);

        if (tipo == TipoMovimentacaoCaixaEnum.SANGRIA) {
            BigDecimal saldo = calcularSaldo(caixa.getId());
            if (saldo.compareTo(valor) < 0) {
                throw new RegraNegocioException("Saldo insuficiente para a sangria: disponível R$ "
                        + saldo.toPlainString() + ", solicitado R$ " + valor.toPlainString());
            }
        }

        MovimentacaoCaixa movimentacao = movimentacaoRepository.save(
                novaMovimentacao(caixa, tipo, valor, textoOuNull(dto.descricao()), usuario));
        return mapper.toDto(movimentacao);
    }

    // ------------------------------------------------------------------ vendas

    /**
     * Usado ao finalizar uma venda: exige caixa aberto e lança nele o dinheiro recebido (já sem o troco).
     * Vendas pagas só com Pix ou cartão também exigem o caixa aberto, mas não geram lançamento.
     */
    @Transactional
    public Caixa registrarVenda(Long vendaId, BigDecimal valorEmDinheiro, UsuarioAutenticado usuario) {
        Caixa aberto = caixaRepository.findByStatus(StatusCaixaEnum.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Abra o caixa antes de finalizar a venda"));
        Caixa caixa = buscarAbertoParaAtualizar(aberto.getId());

        if (valorEmDinheiro.signum() > 0) {
            MovimentacaoCaixa entrada = novaMovimentacao(caixa, TipoMovimentacaoCaixaEnum.VENDA,
                    valorEmDinheiro, "Venda #" + vendaId, usuario);
            entrada.setVendaId(vendaId);
            movimentacaoRepository.save(entrada);
        }
        return caixa;
    }

    // ------------------------------------------------------------------ fechamento

    @Transactional
    public CaixaDto fechar(Long caixaId, FechamentoCaixaDto dto, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        Caixa caixa = buscarAbertoParaAtualizar(caixaId);

        BigDecimal esperado = calcularSaldo(caixa.getId());
        BigDecimal contado = dto.valorContado().setScale(2);
        BigDecimal diferenca = contado.subtract(esperado);

        if (diferenca.signum() != 0 && (dto.observacao() == null || dto.observacao().isBlank())) {
            throw new RegraNegocioException("Informe a justificativa da diferença de R$ "
                    + diferenca.toPlainString() + " no fechamento");
        }

        caixa.setStatus(StatusCaixaEnum.FECHADO);
        caixa.setAbertoUnico(null);
        caixa.setUsuarioFechamento(usuario.username());
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setValorEsperado(esperado);
        caixa.setValorContado(contado);
        caixa.setDiferenca(diferenca);
        caixa.setObservacaoFechamento(textoOuNull(dto.observacao()));

        return mapper.toDto(caixaRepository.save(caixa), esperado);
    }

    // ------------------------------------------------------------------ consultas

    /** Caixa aberto no momento, se houver. Disponível para qualquer usuário autenticado. */
    @Transactional(readOnly = true)
    public Optional<CaixaDto> buscarAtual() {
        return caixaRepository.findByStatus(StatusCaixaEnum.ABERTO).map(this::paraDto);
    }

    @Transactional(readOnly = true)
    public CaixaDto buscarPorId(Long id, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        return paraDto(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PaginaDto<CaixaDto> listar(LocalDate inicio, LocalDate fim, String usuarioFiltro,
            StatusCaixaEnum status, int pagina, int tamanho, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), limitarTamanho(tamanho),
                Sort.by(Sort.Direction.DESC, "dataAbertura", "id"));
        return PaginaDto.de(caixaRepository
                .findAll(CaixaRepository.comFiltros(inicio, fim, usuarioFiltro, status), pageable)
                .map(this::paraDto));
    }

    @Transactional(readOnly = true)
    public PaginaDto<MovimentacaoCaixaDto> listarMovimentacoes(FiltroLancamentosDto filtro, int pagina,
            int tamanho, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), limitarTamanho(tamanho),
                Sort.by(Sort.Direction.DESC, "dataMovimentacao", "id"));
        return PaginaDto.de(movimentacaoRepository
                .findAll(MovimentacaoCaixaRepository.comFiltros(filtro), pageable)
                .map(mapper::toDto));
    }

    /** Totais de entradas e saídas dos lançamentos filtrados (não depende da página). */
    @Transactional(readOnly = true)
    public ResumoLancamentosDto resumirMovimentacoes(FiltroLancamentosDto filtro, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        List<TotalPorTipoDto> totais = movimentacaoRepository
                .totaisPorTipo(MovimentacaoCaixaRepository.comFiltros(filtro));
        BigDecimal entradas = somar(totais, true);
        BigDecimal saidas = somar(totais, false);
        return new ResumoLancamentosDto(entradas, saidas, entradas.subtract(saidas));
    }

    @Transactional(readOnly = true)
    public RelatorioFechamentoDto gerarRelatorioFechamento(Long caixaId, UsuarioAutenticado usuario) {
        exigirGestao(usuario);
        Caixa caixa = buscarEntidade(caixaId);
        if (caixa.getStatus() != StatusCaixaEnum.FECHADO) {
            throw new RegraNegocioException("O relatório só está disponível após o fechamento do caixa");
        }

        List<TotalPorTipoDto> totais = movimentacaoRepository.totaisPorTipo(
                MovimentacaoCaixaRepository.comFiltros(filtroDoCaixa(caixaId)));
        List<MovimentacaoCaixaDto> movimentacoes = movimentacaoRepository
                .findByCaixaIdOrderByDataMovimentacaoAscIdAsc(caixaId).stream()
                .map(mapper::toDto)
                .toList();

        return new RelatorioFechamentoDto(
                mapper.toDto(caixa, caixa.getValorEsperado()),
                totalDoTipo(totais, TipoMovimentacaoCaixaEnum.SUPRIMENTO),
                totalDoTipo(totais, TipoMovimentacaoCaixaEnum.SANGRIA),
                totalDoTipo(totais, TipoMovimentacaoCaixaEnum.VENDA),
                movimentacoes);
    }

    // ------------------------------------------------------------------ apoio

    private void exigirGestao(UsuarioAutenticado usuario) {
        if (!usuario.gestao()) {
            throw new AccessDeniedException("Operação permitida apenas para Administrador ou Gerente");
        }
    }

    private Caixa buscarEntidade(Long id) {
        return caixaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Caixa", id));
    }

    // Lock pessimista na linha do caixa: sangrias e fechamento simultâneos são serializados
    private Caixa buscarAbertoParaAtualizar(Long id) {
        Caixa caixa = caixaRepository.buscarPorIdParaAtualizar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Caixa", id));
        if (caixa.getStatus() != StatusCaixaEnum.ABERTO) {
            throw new RegraNegocioException("O caixa já está fechado");
        }
        return caixa;
    }

    private CaixaDto paraDto(Caixa caixa) {
        return mapper.toDto(caixa, saldoDe(caixa));
    }

    // Caixa fechado reaproveita o valor gravado no fechamento, sem nova consulta
    private BigDecimal saldoDe(Caixa caixa) {
        return caixa.getStatus() == StatusCaixaEnum.FECHADO
                ? caixa.getValorEsperado()
                : calcularSaldo(caixa.getId());
    }

    private BigDecimal calcularSaldo(Long caixaId) {
        List<TotalPorTipoDto> totais = movimentacaoRepository.totaisPorTipo(
                MovimentacaoCaixaRepository.comFiltros(filtroDoCaixa(caixaId)));
        return somar(totais, true).subtract(somar(totais, false));
    }

    private static FiltroLancamentosDto filtroDoCaixa(Long caixaId) {
        return new FiltroLancamentosDto(null, null, null, null, caixaId);
    }

    private static BigDecimal somar(List<TotalPorTipoDto> totais, boolean entrada) {
        return totais.stream()
                .filter(total -> total.tipo().isEntrada() == entrada)
                .map(TotalPorTipoDto::total)
                .reduce(ZERO, BigDecimal::add);
    }

    private static BigDecimal totalDoTipo(List<TotalPorTipoDto> totais, TipoMovimentacaoCaixaEnum tipo) {
        return totais.stream()
                .filter(total -> total.tipo() == tipo)
                .map(TotalPorTipoDto::total)
                .findFirst()
                .orElse(ZERO);
    }

    private static MovimentacaoCaixa novaMovimentacao(Caixa caixa, TipoMovimentacaoCaixaEnum tipo,
            BigDecimal valor, String descricao, UsuarioAutenticado usuario) {
        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
        movimentacao.setCaixa(caixa);
        movimentacao.setTipo(tipo);
        movimentacao.setValor(valor);
        movimentacao.setDescricao(descricao);
        movimentacao.setUsuario(usuario.username());
        return movimentacao;
    }

    private static int limitarTamanho(int tamanho) {
        return Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA);
    }

    private static String textoOuNull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
