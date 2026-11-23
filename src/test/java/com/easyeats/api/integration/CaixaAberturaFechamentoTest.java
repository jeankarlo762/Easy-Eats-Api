package com.easyeats.api.integration;

import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.StatusCaixaEnum;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.support.CaixaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Abertura e fechamento do Caixa.
 *
 * Regras: um único caixa aberto por vez; o fechamento calcula o valor esperado
 * (fundo + suprimentos - sangrias) e exige justificativa quando há diferença;
 * apenas Administrador e Gerente fecham o caixa.
 */
@DisplayName("Caixa: abertura e fechamento")
class CaixaAberturaFechamentoTest extends CaixaTestBase {

    // ------------------------------------------------------------------ abertura

    @Test
    @DisplayName("abertura válida: 201, caixa ABERTO com usuário do token e lançamento do fundo inicial")
    void aberturaValida() throws Exception {
        var resposta = post("/caixa/abrir", "{\"fundoCaixa\": 100.50, \"observacao\": \"Turno da manhã\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.fundoCaixa").value(100.5))
                .andExpect(jsonPath("$.saldoAtual").value(100.5))
                .andExpect(jsonPath("$.usuarioAbertura").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.observacaoAbertura").value("Turno da manhã"))
                .andExpect(jsonPath("$.dataAbertura").isNotEmpty())
                .andExpect(jsonPath("$.dataFechamento").doesNotExist());

        Caixa salvo = caixaRepository.findById(idDaResposta(resposta)).orElseThrow();
        assertThat(salvo.getStatus()).isEqualTo(StatusCaixaEnum.ABERTO);
        assertThat(salvo.getAbertoUnico()).isTrue();

        var lancamentos = movimentacaoCaixaRepository.findByCaixaIdOrderByDataMovimentacaoAscIdAsc(salvo.getId());
        assertThat(lancamentos).hasSize(1);
        assertThat(lancamentos.get(0).getTipo()).isEqualTo(TipoMovimentacaoCaixaEnum.FUNDO_INICIAL);
        assertThat(lancamentos.get(0).getValor()).isEqualByComparingTo("100.50");
        assertThat(lancamentos.get(0).getUsuario()).isEqualTo(USUARIO_PADRAO);
    }

    @Test
    @DisplayName("abertura com fundo zero: 201 e nenhum lançamento de fundo")
    void aberturaComFundoZero() throws Exception {
        Long id = abrirCaixa("0");

        assertThat(movimentacaoCaixaRepository.findByCaixaIdOrderByDataMovimentacaoAscIdAsc(id)).isEmpty();
        get("/caixa/atual").andExpect(status().isOk()).andExpect(jsonPath("$.saldoAtual").value(0.0));
    }

    @Test
    @DisplayName("funcionário também pode abrir o caixa")
    void funcionarioPodeAbrir() throws Exception {
        String token = tokenDe("func.teste", "FUNCIONARIO");

        postComo(token, "/caixa/abrir", jsonAbertura("50"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioAbertura").value("func.teste"));
    }

    static Stream<Arguments> fundosInvalidos() {
        return Stream.of(
                Arguments.of("fundo ausente", "{}", "fundoCaixa: O fundo de caixa é obrigatório"),
                Arguments.of("fundo negativo", jsonAbertura("-1"), "fundoCaixa: O fundo de caixa não pode ser negativo"),
                Arguments.of("3 casas decimais", jsonAbertura("10.123"), "fundoCaixa: Use no máximo 2 casas decimais")
        );
    }

    @ParameterizedTest(name = "{0} -> 400 e nada é gravado")
    @MethodSource("fundosInvalidos")
    void fundoInvalido(String cenario, String corpo, String campoEsperado) throws Exception {
        assertErroValidacao(post("/caixa/abrir", corpo), campoEsperado);
        assertThat(caixaRepository.count()).isZero();
    }

    @Test
    @DisplayName("abrir com outro caixa aberto: 422 e só o primeiro permanece")
    void jaExisteCaixaAberto() throws Exception {
        abrirCaixa("100");

        assertRegraNegocio(post("/caixa/abrir", jsonAbertura("200")), "Já existe um caixa aberto");
        assertThat(caixaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("o banco impede dois caixas abertos mesmo sem passar pela regra do service")
    void bancoImpedeDoisCaixasAbertos() throws Exception {
        abrirCaixa("100");

        assertThatThrownBy(() -> salvarCaixa(StatusCaixaEnum.ABERTO, "outra.pessoa", LocalDateTime.now()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(caixaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("vários caixas fechados convivem (restrição de abertura só vale para o aberto)")
    void variosCaixasFechados() throws Exception {
        salvarCaixa(StatusCaixaEnum.FECHADO, "ana", LocalDateTime.now().minusDays(2));
        salvarCaixa(StatusCaixaEnum.FECHADO, "bia", LocalDateTime.now().minusDays(1));

        post("/caixa/abrir", jsonAbertura("100")).andExpect(status().isCreated());
        assertThat(caixaRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("sem token: requisição barrada pela segurança")
    void semToken() throws Exception {
        assertBarradoPelaSeguranca(postSemToken("/caixa/abrir", jsonAbertura("100")));
        assertThat(caixaRepository.count()).isZero();
    }

    // ------------------------------------------------------------------ caixa atual

    @Test
    @DisplayName("sem caixa aberto: GET /caixa/atual responde 204")
    void atualSemCaixa() throws Exception {
        get("/caixa/atual").andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("com caixa aberto: GET /caixa/atual devolve o caixa com o saldo, inclusive para funcionário")
    void atualComCaixa() throws Exception {
        Long id = abrirCaixa("100");
        lancar(id, "SUPRIMENTO", "25.50", "Reforço de troco");

        getComo(tokenDe("func.teste", "FUNCIONARIO"), "/caixa/atual")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.saldoAtual").value(125.5));
    }

    // ------------------------------------------------------------------ fechamento

    @Test
    @DisplayName("fechamento sem diferença: 200, valores gravados e caixa liberado para nova abertura")
    void fechamentoSemDiferenca() throws Exception {
        Long id = abrirCaixa("100");

        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"))
                .andExpect(jsonPath("$.valorEsperado").value(100.0))
                .andExpect(jsonPath("$.valorContado").value(100.0))
                .andExpect(jsonPath("$.diferenca").value(0.0))
                .andExpect(jsonPath("$.usuarioFechamento").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.dataFechamento").isNotEmpty());

        Caixa salvo = caixaRepository.findById(id).orElseThrow();
        assertThat(salvo.getStatus()).isEqualTo(StatusCaixaEnum.FECHADO);
        assertThat(salvo.getAbertoUnico()).isNull();

        get("/caixa/atual").andExpect(status().isNoContent());
        post("/caixa/abrir", jsonAbertura("80")).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("fechamento considera fundo, suprimentos e sangrias no valor esperado")
    void esperadoConsideraMovimentacoes() throws Exception {
        Long id = abrirCaixa("100");
        lancar(id, "SUPRIMENTO", "50", "Reforço");
        lancar(id, "SANGRIA", "30", "Pagamento de fornecedor");

        post("/caixa/%d/fechar".formatted(id), jsonFechamento("120", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorEsperado").value(120.0))
                .andExpect(jsonPath("$.diferenca").value(0.0));
    }

    @Test
    @DisplayName("sobra com justificativa: 200 e diferença positiva")
    void sobraComJustificativa() throws Exception {
        Long id = abrirCaixa("100");

        post("/caixa/%d/fechar".formatted(id), jsonFechamento("105", "Gorjeta deixada no caixa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diferenca").value(5.0))
                .andExpect(jsonPath("$.observacaoFechamento").value("Gorjeta deixada no caixa"));
    }

    @Test
    @DisplayName("falta com justificativa: 200 e diferença negativa")
    void faltaComJustificativa() throws Exception {
        Long id = abrirCaixa("100");

        post("/caixa/%d/fechar".formatted(id), jsonFechamento("92.50", "Troco errado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diferenca").value(-7.5));
    }

    @Test
    @DisplayName("diferença sem justificativa: 422 e o caixa continua aberto")
    void diferencaSemJustificativa() throws Exception {
        Long id = abrirCaixa("100");

        assertRegraNegocio(post("/caixa/%d/fechar".formatted(id), jsonFechamento("105", "")),
                "Informe a justificativa da diferença de R$ 5.00 no fechamento");
        assertRegraNegocio(post("/caixa/%d/fechar".formatted(id), "{\"valorContado\": 90}"),
                "Informe a justificativa da diferença de R$ -10.00 no fechamento");

        Caixa salvo = caixaRepository.findById(id).orElseThrow();
        assertThat(salvo.getStatus()).isEqualTo(StatusCaixaEnum.ABERTO);
        assertThat(salvo.getValorContado()).isNull();
    }

    static Stream<Arguments> contadosInvalidos() {
        return Stream.of(
                Arguments.of("valor ausente", "{}", "valorContado: O valor contado é obrigatório"),
                Arguments.of("valor negativo", "{\"valorContado\": -5}", "valorContado: O valor contado não pode ser negativo"),
                Arguments.of("3 casas decimais", "{\"valorContado\": 10.555}", "valorContado: Use no máximo 2 casas decimais")
        );
    }

    @ParameterizedTest(name = "{0} -> 400 e o caixa continua aberto")
    @MethodSource("contadosInvalidos")
    void contadoInvalido(String cenario, String corpo, String campoEsperado) throws Exception {
        Long id = abrirCaixa("100");

        assertErroValidacao(post("/caixa/%d/fechar".formatted(id), corpo), campoEsperado);
        assertThat(caixaRepository.findById(id).orElseThrow().getStatus()).isEqualTo(StatusCaixaEnum.ABERTO);
    }

    @Test
    @DisplayName("funcionário não pode fechar o caixa: 403 e o caixa continua aberto")
    void funcionarioNaoFecha() throws Exception {
        Long id = abrirCaixa("100");

        postComo(tokenDe("func.teste", "FUNCIONARIO"), "/caixa/%d/fechar".formatted(id), jsonFechamento("100", ""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida apenas para Administrador ou Gerente"));

        assertThat(caixaRepository.findById(id).orElseThrow().getStatus()).isEqualTo(StatusCaixaEnum.ABERTO);
    }

    @Test
    @DisplayName("gerente pode fechar o caixa e fica registrado como responsável pelo fechamento")
    void gerenteFecha() throws Exception {
        Long id = abrirCaixa("100");

        postComo(tokenDe("gerente.teste", "GERENTE"), "/caixa/%d/fechar".formatted(id), jsonFechamento("100", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioAbertura").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.usuarioFechamento").value("gerente.teste"));
    }

    @Test
    @DisplayName("fechar um caixa já fechado: 422")
    void fecharDuasVezes() throws Exception {
        Long id = abrirCaixa("100");
        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")).andExpect(status().isOk());

        assertRegraNegocio(post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")),
                "O caixa já está fechado");
    }

    @Test
    @DisplayName("fechar caixa inexistente: 404")
    void fecharInexistente() throws Exception {
        assertNaoEncontrado(post("/caixa/%d/fechar".formatted(ID_INEXISTENTE), jsonFechamento("100", "")),
                "Caixa não encontrado: " + ID_INEXISTENTE);
    }

    @Test
    @DisplayName("valor esperado gravado no fechamento não muda depois")
    void esperadoGravadoNoFechamento() throws Exception {
        Long id = abrirCaixa("100");
        post("/caixa/%d/fechar".formatted(id), jsonFechamento("100", "")).andExpect(status().isOk());

        Caixa salvo = caixaRepository.findById(id).orElseThrow();
        assertThat(salvo.getValorEsperado()).isEqualByComparingTo(new BigDecimal("100.00"));
        get("/caixa/%d".formatted(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(100.0));
    }
}
