package com.easyeats.api.integration;

import com.easyeats.api.entity.MovimentacaoCaixa;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.support.CaixaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Suprimento e sangria.
 *
 * Regras: só em caixa aberto; sangria exige motivo, não pode passar do saldo e é restrita
 * a Administrador e Gerente; suprimento é liberado para qualquer usuário autenticado; o usuário
 * gravado vem sempre do token.
 */
@DisplayName("Caixa: suprimento e sangria")
class CaixaMovimentacaoTest extends CaixaTestBase {

    private String url(Long caixaId) {
        return "/caixa/%d/movimentacoes".formatted(caixaId);
    }

    private List<MovimentacaoCaixa> lancamentos(Long caixaId) {
        return movimentacaoCaixaRepository.findByCaixaIdOrderByDataMovimentacaoAscIdAsc(caixaId);
    }

    // ------------------------------------------------------------------ suprimento

    @Test
    @DisplayName("suprimento válido: 201, soma no saldo e grava o usuário do token")
    void suprimentoValido() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), jsonMovimentacao("SUPRIMENTO", "40.25", "Reforço de troco"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.caixaId").value(id))
                .andExpect(jsonPath("$.tipo").value("SUPRIMENTO"))
                .andExpect(jsonPath("$.valor").value(40.25))
                .andExpect(jsonPath("$.descricao").value("Reforço de troco"))
                .andExpect(jsonPath("$.usuario").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.dataMovimentacao").isNotEmpty());

        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(140.25));
    }

    @Test
    @DisplayName("suprimento sem descrição é aceito")
    void suprimentoSemDescricao() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), "{\"tipo\": \"SUPRIMENTO\", \"valor\": 10}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    @DisplayName("funcionário pode lançar suprimento")
    void funcionarioLancaSuprimento() throws Exception {
        Long id = abrirCaixa("100");

        postComo(tokenDe("func.teste", "FUNCIONARIO"), url(id), jsonMovimentacao("SUPRIMENTO", "10", "Troco"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value("func.teste"));
    }

    @Test
    @DisplayName("usuário, data e caixa enviados no corpo são ignorados")
    void camposDaApiSaoIgnorados() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), "{\"tipo\": \"SUPRIMENTO\", \"valor\": 10, \"usuario\": \"outra.pessoa\", "
                + "\"caixaId\": 999999, \"dataMovimentacao\": \"2000-01-01T00:00:00\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.caixaId").value(id));

        MovimentacaoCaixa salva = lancamentos(id).get(lancamentos(id).size() - 1);
        assertThat(salva.getDataMovimentacao().getYear()).isGreaterThan(2000);
    }

    // ------------------------------------------------------------------ sangria

    @Test
    @DisplayName("sangria válida: 201 e subtrai do saldo")
    void sangriaValida() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), jsonMovimentacao("SANGRIA", "30", "Pagamento de fornecedor"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SANGRIA"))
                .andExpect(jsonPath("$.usuario").value(USUARIO_PADRAO));

        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(70.0));
    }

    @Test
    @DisplayName("sangria do valor exato do saldo é permitida e zera o caixa")
    void sangriaDoSaldoInteiro() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), jsonMovimentacao("SANGRIA", "100", "Recolhimento ao cofre")).andExpect(status().isCreated());

        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(0.0));
    }

    @Test
    @DisplayName("sangria maior que o saldo: 422 e nada é gravado")
    void sangriaMaiorQueSaldo() throws Exception {
        Long id = abrirCaixa("100");

        assertRegraNegocio(post(url(id), jsonMovimentacao("SANGRIA", "150", "Pagamento")),
                "Saldo insuficiente para a sangria: disponível R$ 100.00, solicitado R$ 150.00");

        assertThat(lancamentos(id)).hasSize(1);
    }

    @Test
    @DisplayName("sangria sem motivo: 422 e nada é gravado")
    void sangriaSemMotivo() throws Exception {
        Long id = abrirCaixa("100");

        assertRegraNegocio(post(url(id), "{\"tipo\": \"SANGRIA\", \"valor\": 10}"), "Informe o motivo da sangria");
        assertRegraNegocio(post(url(id), jsonMovimentacao("SANGRIA", "10", "   ")), "Informe o motivo da sangria");

        assertThat(lancamentos(id)).hasSize(1);
    }

    @Test
    @DisplayName("funcionário não pode fazer sangria: 403 e nada é gravado")
    void funcionarioNaoFazSangria() throws Exception {
        Long id = abrirCaixa("100");

        postComo(tokenDe("func.teste", "FUNCIONARIO"), url(id), jsonMovimentacao("SANGRIA", "10", "Pagamento"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida apenas para Administrador ou Gerente"));

        assertThat(lancamentos(id)).hasSize(1);
    }

    @Test
    @DisplayName("gerente pode fazer sangria")
    void gerenteFazSangria() throws Exception {
        Long id = abrirCaixa("100");

        postComo(tokenDe("gerente.teste", "GERENTE"), url(id), jsonMovimentacao("SANGRIA", "10", "Pagamento"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value("gerente.teste"));
    }

    // ------------------------------------------------------------------ validações

    @Test
    @DisplayName("fundo inicial não pode ser lançado pela API: 422")
    void fundoInicialNaoPermitido() throws Exception {
        Long id = abrirCaixa("100");

        assertRegraNegocio(post(url(id), jsonMovimentacao("FUNDO_INICIAL", "10", "Fundo")),
                "O fundo de caixa só pode ser informado na abertura");
        assertThat(lancamentos(id)).hasSize(1);
    }

    static Stream<Arguments> corposInvalidos() {
        return Stream.of(
                Arguments.of("valor ausente", "{\"tipo\": \"SUPRIMENTO\"}", "valor: O valor é obrigatório"),
                Arguments.of("valor zero", "{\"tipo\": \"SUPRIMENTO\", \"valor\": 0}", "valor: O valor deve ser maior que zero"),
                Arguments.of("valor negativo", "{\"tipo\": \"SUPRIMENTO\", \"valor\": -5}", "valor: O valor deve ser maior que zero"),
                Arguments.of("3 casas decimais", "{\"tipo\": \"SUPRIMENTO\", \"valor\": 1.005}", "valor: Use no máximo 2 casas decimais"),
                Arguments.of("tipo ausente", "{\"valor\": 10}", "tipo: O tipo é obrigatório")
        );
    }

    @ParameterizedTest(name = "{0} -> 400 e nada é gravado")
    @MethodSource("corposInvalidos")
    void corpoInvalido(String cenario, String corpo, String campoEsperado) throws Exception {
        Long id = abrirCaixa("100");

        assertErroValidacao(post(url(id), corpo), campoEsperado);
        assertThat(lancamentos(id)).hasSize(1);
    }

    @Test
    @DisplayName("tipo inexistente: 400 e nada é gravado")
    void tipoInexistente() throws Exception {
        Long id = abrirCaixa("100");

        post(url(id), "{\"tipo\": \"PRESENTE\", \"valor\": 10}").andExpect(status().isBadRequest());
        assertThat(lancamentos(id)).hasSize(1);
    }

    // ------------------------------------------------------------------ estado do caixa

    @Test
    @DisplayName("lançar em caixa fechado: 422")
    void caixaFechado() throws Exception {
        Long id = abrirCaixa("100");
        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")).andExpect(status().isOk());

        assertRegraNegocio(post(url(id), jsonMovimentacao("SUPRIMENTO", "10", "Reforço")),
                "O caixa já está fechado");
        assertThat(lancamentos(id)).hasSize(1);
    }

    @Test
    @DisplayName("lançar em caixa inexistente: 404")
    void caixaInexistente() throws Exception {
        assertNaoEncontrado(post(url(ID_INEXISTENTE), jsonMovimentacao("SUPRIMENTO", "10", "Reforço")),
                "Caixa não encontrado: " + ID_INEXISTENTE);
    }

    @Test
    @DisplayName("vários lançamentos acumulam corretamente no saldo")
    void saldoAcumulado() throws Exception {
        Long id = abrirCaixa("200");
        lancar(id, "SUPRIMENTO", "50.10", "Reforço 1");
        lancar(id, "SUPRIMENTO", "49.90", "Reforço 2");
        lancar(id, "SANGRIA", "75.55", "Cofre");

        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(224.45));
        assertThat(lancamentos(id)).extracting(MovimentacaoCaixa::getTipo).containsExactly(
                TipoMovimentacaoCaixaEnum.FUNDO_INICIAL,
                TipoMovimentacaoCaixaEnum.SUPRIMENTO,
                TipoMovimentacaoCaixaEnum.SUPRIMENTO,
                TipoMovimentacaoCaixaEnum.SANGRIA);
    }
}
