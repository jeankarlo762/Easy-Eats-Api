package com.easyeats.api.integration;

import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.StatusCaixaEnum;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.support.CaixaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consultas do Caixa: histórico, lançamentos com filtros, resumo e relatório de fechamento.
 * Todas são restritas a Administrador e Gerente.
 */
@DisplayName("Caixa: consultas, filtros e relatório de fechamento")
class CaixaConsultaTest extends CaixaTestBase {

    private static final String MENSAGEM_SEM_PERMISSAO = "Operação permitida apenas para Administrador ou Gerente";

    private static final LocalDateTime DIA_10 = LocalDateTime.of(2026, 3, 10, 9, 0);
    private static final LocalDateTime DIA_11 = LocalDateTime.of(2026, 3, 11, 9, 0);
    private static final LocalDateTime DIA_12 = LocalDateTime.of(2026, 3, 12, 9, 0);

    // ------------------------------------------------------------------ relatório de fechamento

    @Test
    @DisplayName("relatório de fechamento: usuários, totais, diferença e lançamentos em ordem cronológica")
    void relatorioCompleto() throws Exception {
        Long id = abrirCaixa("100");
        lancar(id, "SUPRIMENTO", "50", "Reforço");
        lancar(id, "SANGRIA", "30", "Fornecedor");
        lancar(id, "SANGRIA", "20", "Cofre");
        postComo(tokenDe("gerente.teste", "GERENTE"), "/caixa/%d/fechar".formatted(id),
                jsonFechamento("95", "Falta de 5 no troco")).andExpect(status().isOk());

        get("/caixa/%d/relatorio-fechamento".formatted(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caixa.id").value(id))
                .andExpect(jsonPath("$.caixa.status").value("FECHADO"))
                .andExpect(jsonPath("$.caixa.usuarioAbertura").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.caixa.usuarioFechamento").value("gerente.teste"))
                .andExpect(jsonPath("$.caixa.fundoCaixa").value(100.0))
                .andExpect(jsonPath("$.caixa.valorEsperado").value(100.0))
                .andExpect(jsonPath("$.caixa.valorContado").value(95.0))
                .andExpect(jsonPath("$.caixa.diferenca").value(-5.0))
                .andExpect(jsonPath("$.caixa.observacaoFechamento").value("Falta de 5 no troco"))
                .andExpect(jsonPath("$.totalSuprimentos").value(50.0))
                .andExpect(jsonPath("$.totalSangrias").value(50.0))
                .andExpect(jsonPath("$.movimentacoes.length()").value(4))
                .andExpect(jsonPath("$.movimentacoes[0].tipo").value("FUNDO_INICIAL"))
                .andExpect(jsonPath("$.movimentacoes[1].tipo").value("SUPRIMENTO"))
                .andExpect(jsonPath("$.movimentacoes[2].descricao").value("Fornecedor"))
                .andExpect(jsonPath("$.movimentacoes[3].descricao").value("Cofre"))
                .andExpect(jsonPath("$.movimentacoes[3].usuario").value(USUARIO_PADRAO));
    }

    @Test
    @DisplayName("relatório sem suprimentos nem sangrias: totais zerados")
    void relatorioSemMovimentacoes() throws Exception {
        Long id = abrirCaixa("100");
        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")).andExpect(status().isOk());

        get("/caixa/%d/relatorio-fechamento".formatted(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSuprimentos").value(0.0))
                .andExpect(jsonPath("$.totalSangrias").value(0.0))
                .andExpect(jsonPath("$.movimentacoes.length()").value(1));
    }

    @Test
    @DisplayName("relatório de caixa ainda aberto: 422")
    void relatorioDeCaixaAberto() throws Exception {
        Long id = abrirCaixa("100");

        assertRegraNegocio(get("/caixa/%d/relatorio-fechamento".formatted(id)),
                "O relatório só está disponível após o fechamento do caixa");
    }

    @Test
    @DisplayName("relatório de caixa inexistente: 404")
    void relatorioInexistente() throws Exception {
        assertNaoEncontrado(get("/caixa/%d/relatorio-fechamento".formatted(ID_INEXISTENTE)),
                "Caixa não encontrado: " + ID_INEXISTENTE);
    }

    // ------------------------------------------------------------------ histórico de caixas

    @Test
    @DisplayName("histórico: mais recentes primeiro, com paginação")
    void historicoPaginado() throws Exception {
        salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        salvarCaixa(StatusCaixaEnum.FECHADO, "bia", DIA_11);
        salvarCaixa(StatusCaixaEnum.FECHADO, "caio", DIA_12);

        get("/caixa?pagina=0&tamanho=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].usuarioAbertura").value("caio"))
                .andExpect(jsonPath("$.itens[1].usuarioAbertura").value("bia"));

        get("/caixa?pagina=1&tamanho=2")
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].usuarioAbertura").value("ana"));
    }

    @Test
    @DisplayName("histórico: filtro por período usa a data de abertura, com as duas pontas inclusivas")
    void historicoPorPeriodo() throws Exception {
        salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        salvarCaixa(StatusCaixaEnum.FECHADO, "bia", DIA_11);
        salvarCaixa(StatusCaixaEnum.FECHADO, "caio", DIA_12);

        get("/caixa?inicio=2026-03-11&fim=2026-03-11")
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].usuarioAbertura").value("bia"));
        get("/caixa?inicio=2026-03-11&fim=2026-03-12")
                .andExpect(jsonPath("$.total").value(2));
        get("/caixa?inicio=2026-03-13")
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("histórico: filtro por usuário considera quem abriu e quem fechou, e por status")
    void historicoPorUsuarioEStatus() throws Exception {
        salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        Caixa fechadoPorOutro = salvarCaixa(StatusCaixaEnum.FECHADO, "bia", DIA_11);
        fechadoPorOutro.setUsuarioFechamento("ana");
        caixaRepository.save(fechadoPorOutro);
        salvarCaixa(StatusCaixaEnum.ABERTO, "caio", DIA_12);

        get("/caixa?usuario=ana").andExpect(jsonPath("$.total").value(2));
        get("/caixa?usuario=caio").andExpect(jsonPath("$.total").value(1));
        get("/caixa?status=ABERTO").andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].usuarioAbertura").value("caio"));
        get("/caixa?status=FECHADO&usuario=caio").andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("histórico: tamanho de página acima do limite é reduzido para 100")
    void historicoLimitaTamanho() throws Exception {
        salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);

        get("/caixa?tamanho=5000").andExpect(jsonPath("$.tamanho").value(100));
    }

    @Test
    @DisplayName("buscar caixa por id: 200 para gestão e 404 quando não existe")
    void buscarPorId() throws Exception {
        Long id = abrirCaixa("100");

        get("/caixa/%d".formatted(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.saldoAtual").value(100.0));
        assertNaoEncontrado(get("/caixa/%d".formatted(ID_INEXISTENTE)), "Caixa não encontrado: " + ID_INEXISTENTE);
    }

    // ------------------------------------------------------------------ lançamentos

    @Test
    @DisplayName("lançamentos: mais recentes primeiro, com paginação")
    void lancamentosPaginados() throws Exception {
        Caixa caixa = salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "10", "ana", DIA_10);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "20", "ana", DIA_11);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "30", "ana", DIA_12);

        get("/caixa/movimentacoes?pagina=0&tamanho=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].valor").value(30.0))
                .andExpect(jsonPath("$.itens[1].valor").value(20.0));
    }

    @Test
    @DisplayName("lançamentos: filtros por tipo, usuário, caixa e período, combinados")
    void lancamentosComFiltros() throws Exception {
        Caixa primeiro = salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        Caixa segundo = salvarCaixa(StatusCaixaEnum.FECHADO, "bia", DIA_11);
        salvarMovimentacao(primeiro, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "10", "ana", DIA_10);
        salvarMovimentacao(primeiro, TipoMovimentacaoCaixaEnum.SANGRIA, "5", "ana", DIA_10);
        salvarMovimentacao(segundo, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "20", "bia", DIA_11);
        salvarMovimentacao(segundo, TipoMovimentacaoCaixaEnum.SANGRIA, "8", "bia", DIA_12);

        get("/caixa/movimentacoes").andExpect(jsonPath("$.total").value(4));
        get("/caixa/movimentacoes?tipo=SANGRIA").andExpect(jsonPath("$.total").value(2));
        get("/caixa/movimentacoes?usuario=bia").andExpect(jsonPath("$.total").value(2));
        get("/caixa/movimentacoes?caixaId=" + primeiro.getId()).andExpect(jsonPath("$.total").value(2));
        get("/caixa/movimentacoes?inicio=2026-03-11&fim=2026-03-11").andExpect(jsonPath("$.total").value(1));
        get("/caixa/movimentacoes?inicio=2026-03-11").andExpect(jsonPath("$.total").value(2));
        get("/caixa/movimentacoes?tipo=SANGRIA&usuario=bia&caixaId=" + segundo.getId())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].valor").value(8.0));
        get("/caixa/movimentacoes?tipo=SUPRIMENTO&usuario=bia&caixaId=" + primeiro.getId())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("lançamentos: tipo inválido no filtro responde 422 (handler de IllegalArgumentException)")
    void lancamentosTipoInvalido() throws Exception {
        get("/caixa/movimentacoes?tipo=PRESENTE").andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("resumo: totais de entradas, saídas e saldo respeitam os filtros e não dependem da página")
    void resumoDosLancamentos() throws Exception {
        Caixa caixa = salvarCaixa(StatusCaixaEnum.FECHADO, "ana", DIA_10);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.FUNDO_INICIAL, "100", "ana", DIA_10);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SUPRIMENTO, "50.50", "ana", DIA_10);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SANGRIA, "30.25", "ana", DIA_11);
        salvarMovimentacao(caixa, TipoMovimentacaoCaixaEnum.SANGRIA, "10", "bia", DIA_12);

        get("/caixa/movimentacoes/resumo")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEntradas").value(150.5))
                .andExpect(jsonPath("$.totalSaidas").value(40.25))
                .andExpect(jsonPath("$.saldo").value(110.25));
        get("/caixa/movimentacoes/resumo?tipo=SANGRIA")
                .andExpect(jsonPath("$.totalEntradas").value(0.0))
                .andExpect(jsonPath("$.totalSaidas").value(40.25))
                .andExpect(jsonPath("$.saldo").value(-40.25));
        get("/caixa/movimentacoes/resumo?inicio=2026-03-11&fim=2026-03-11&usuario=ana")
                .andExpect(jsonPath("$.totalSaidas").value(30.25));
        get("/caixa/movimentacoes/resumo?inicio=2030-01-01")
                .andExpect(jsonPath("$.totalEntradas").value(0.0))
                .andExpect(jsonPath("$.totalSaidas").value(0.0))
                .andExpect(jsonPath("$.saldo").value(0.0));
    }

    // ------------------------------------------------------------------ permissões

    @Test
    @DisplayName("funcionário não acessa histórico, lançamentos, resumo, detalhe nem relatório: 403")
    void funcionarioNaoConsulta() throws Exception {
        Long id = abrirCaixa("100");
        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")).andExpect(status().isOk());
        String funcionario = tokenDe("func.teste", "FUNCIONARIO");

        for (String url : new String[] {
                "/caixa",
                "/caixa/%d".formatted(id),
                "/caixa/movimentacoes",
                "/caixa/movimentacoes/resumo",
                "/caixa/%d/relatorio-fechamento".formatted(id)}) {
            getComo(funcionario, url)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.erro").value(MENSAGEM_SEM_PERMISSAO));
        }
    }

    @Test
    @DisplayName("gerente acessa as consultas de gestão")
    void gerenteConsulta() throws Exception {
        abrirCaixa("100");
        String gerente = tokenDe("gerente.teste", "GERENTE");

        getComo(gerente, "/caixa").andExpect(status().isOk());
        getComo(gerente, "/caixa/movimentacoes").andExpect(status().isOk());
        getComo(gerente, "/caixa/movimentacoes/resumo").andExpect(status().isOk());
    }

    @Test
    @DisplayName("período do dia atual: lançamentos criados agora aparecem no filtro de hoje")
    void filtroDeHoje() throws Exception {
        Long id = abrirCaixa("100");
        lancar(id, "SUPRIMENTO", "10", "Reforço");
        String hoje = LocalDate.now().toString();

        get("/caixa/movimentacoes?inicio=%s&fim=%s".formatted(hoje, hoje))
                .andExpect(jsonPath("$.total").value(2));
        get("/caixa?inicio=%s&fim=%s".formatted(hoje, hoje))
                .andExpect(jsonPath("$.total").value(1));
    }
}
