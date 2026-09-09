package com.easy.eats.caixa.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.caixa.enums.StatusCaixa;
import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.caixa.repository.CaixaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.movimentacaoFinanceira.enums.TipoMovimentacao;
import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.repository.MovimentacaoFinanceiraRepository;
import com.easy.eats.pagamento.repository.PagamentoRepository;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.usuario.repository.UsuarioRepository;

@Service
public class CaixaService {

    @Autowired
    CaixaRepository repository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PagamentoRepository pagamentoRepository;

    @Autowired
    MovimentacaoFinanceiraRepository movimentacaoRepository;

    /** Caixa aberto da empresa, ou null se nenhum estiver aberto agora. */
    public Caixa status() {
        return repository.findByEmpresaIdAndStatus(SecurityUtils.getEmpresaId(), StatusCaixa.ABERTO).orElse(null);
    }

    @Transactional
    public Caixa abrir(BigDecimal valorInicial, String observacoes) {
        Integer empresaId = SecurityUtils.getEmpresaId();

        if (repository.findByEmpresaIdAndStatus(empresaId, StatusCaixa.ABERTO).isPresent()) {
            throw new IllegalArgumentException("Já existe um caixa aberto para esta empresa");
        }
        if (valorInicial == null || valorInicial.signum() < 0) {
            throw new IllegalArgumentException("Informe o valor inicial do caixa");
        }

        Caixa caixa = new Caixa();
        caixa.setStatus(StatusCaixa.ABERTO);
        caixa.setValorInicial(valorInicial);
        caixa.setObservacoesAbertura(observacoes);
        caixa.setDtAbertura(LocalDateTime.now());
        caixa.setUsuarioAbertura(
                usuarioRepository.getReferenceById(SecurityUtils.getUsuarioAutenticado().getUsuarioId()));
        caixa.setEmpresa(empresaRepository.getReferenceById(empresaId));

        return repository.save(caixa);
    }

    /**
     * O apurado do sistema é calculado a partir da soma de pagamentos e
     * movimentações lidos aqui — sem transação, um pagamento registrado no
     * instante exato do fechamento poderia entrar na leitura mas não refletir
     * de forma consistente no restante do cálculo.
     */
    @Transactional
    public Caixa fechar(Integer id, BigDecimal valorApuradoInformado, String observacoes) {
        Caixa caixa = buscarPorId(id);
        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new IllegalArgumentException("Este caixa já está fechado");
        }

        BigDecimal totalPagamentos = pagamentoRepository.findAllByCaixaId(id).stream()
                .map(p -> p.getValor() != null ? p.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSuprimento = BigDecimal.ZERO;
        BigDecimal totalSangria = BigDecimal.ZERO;
        for (MovimentacaoFinanceira mov : movimentacaoRepository.findAllByCaixaId(id)) {
            BigDecimal valor = mov.getValor() != null ? mov.getValor() : BigDecimal.ZERO;
            if (mov.getTipo() == TipoMovimentacao.SUPRIMENTO) {
                totalSuprimento = totalSuprimento.add(valor);
            } else if (mov.getTipo() == TipoMovimentacao.SANGRIA) {
                totalSangria = totalSangria.add(valor);
            }
        }

        BigDecimal valorApuradoSistema = caixa.getValorInicial()
                .add(totalPagamentos)
                .add(totalSuprimento)
                .subtract(totalSangria);

        caixa.setValorApuradoSistema(valorApuradoSistema);
        caixa.setValorApuradoInformado(valorApuradoInformado);
        caixa.setDiferenca(valorApuradoInformado != null ? valorApuradoInformado.subtract(valorApuradoSistema) : null);
        caixa.setObservacoesFechamento(observacoes);
        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setDtFechamento(LocalDateTime.now());
        caixa.setUsuarioFechamento(
                usuarioRepository.getReferenceById(SecurityUtils.getUsuarioAutenticado().getUsuarioId()));

        return repository.save(caixa);
    }

    @Transactional
    public MovimentacaoFinanceira registrarMovimentacao(Integer caixaId, String tipo, BigDecimal valor,
            String descricao) {
        Caixa caixa = buscarPorId(caixaId);
        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new IllegalArgumentException("O caixa não está aberto");
        }
        TipoMovimentacao tipoMovimentacao = tipoMovimentacaoDoTexto(tipo);
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Informe um valor maior que zero");
        }

        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira();
        movimentacao.setTipo(tipoMovimentacao);
        movimentacao.setCategoria("CAIXA");
        movimentacao.setValor(valor);
        movimentacao.setDescricao(descricao);
        movimentacao.setCaixa(caixa);
        movimentacao.setEmpresa(caixa.getEmpresa());

        return movimentacaoRepository.save(movimentacao);
    }

    private TipoMovimentacao tipoMovimentacaoDoTexto(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de movimentação inválido, use SANGRIA ou SUPRIMENTO");
        }
        try {
            return TipoMovimentacao.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException semCorrespondencia) {
            throw new IllegalArgumentException("Tipo de movimentação inválido, use SANGRIA ou SUPRIMENTO");
        }
    }

    private Caixa buscarPorId(Integer id) {
        Caixa caixa = SecurityUtils.isSuperadmin()
                ? repository.findById(id).orElse(null)
                : repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId()).orElse(null);

        if (caixa == null) {
            throw new RuntimeException("Caixa não encontrado");
        }
        return caixa;
    }
}
