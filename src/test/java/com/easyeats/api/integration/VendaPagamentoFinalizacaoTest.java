package com.easyeats.api.integration;

import com.easyeats.api.entity.MovimentacaoCaixa;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import com.easyeats.api.support.VendaTestBase;
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
 * Pagamento e finalização da venda.
 *
 * Regras: pagamentos podem ser lançados a qualquer momento enquanto a venda está aberta (inclusive
 * antes de a cozinha terminar) e nunca passam do total; a venda só finaliza com a soma dos pagamentos
 * igual ao total e com caixa aberto; só o dinheiro (sem o troco) entra no caixa; a mesa só é liberada
 * na finalização.
 */
@DisplayName("Venda: pagamentos, finalização e caixa")
class VendaPagamentoFinalizacaoTest extends VendaTestBase {

    private List<MovimentacaoCaixa> entradasDeVenda(Long caixaId) {
        return movimentacaoCaixaRepository.findByCaixaIdOrderByDataMovimentacaoAscIdAsc(caixaId).stream()
                .filter(m -> m.getTipo() == TipoMovimentacaoCaixaEnum.VENDA)
                .toList();
    }

    // ------------------------------------------------------------------ pagamentos

    @Test
    @DisplayName("pagamento em dinheiro calcula o troco e atualiza o valor pago e o restante")
    void pagamentoEmDinheiroComTroco() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        pagarEmDinheiro(vendaId, "20.00", "50.00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorPago").value(20.0))
                .andExpect(jsonPath("$.valorRestante").value(5.9))
                .andExpect(jsonPath("$.pagamentos.length()").value(1))
                .andExpect(jsonPath("$.pagamentos[0].forma").value("DINHEIRO"))
                .andExpect(jsonPath("$.pagamentos[0].valor").value(20.0))
                .andExpect(jsonPath("$.pagamentos[0].valorRecebido").value(50.0))
                .andExpect(jsonPath("$.pagamentos[0].troco").value(30.0))
                .andExpect(jsonPath("$.pagamentos[0].usuario").value(USUARIO_PADRAO));
    }

    @Test
    @DisplayName("dinheiro sem valor recebido assume o próprio valor, sem troco")
    void dinheiroSemValorRecebido() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        pagar(vendaId, "DINHEIRO", "10.00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pagamentos[0].valorRecebido").value(10.0))
                .andExpect(jsonPath("$.pagamentos[0].troco").value(0.0));
    }

    @Test
    @DisplayName("Pix e cartão não têm valor recebido nem troco, mesmo que o cliente envie")
    void formasSemTroco() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        post("/vendas/%d/pagamentos".formatted(vendaId), "{\"forma\": \"PIX\", \"valor\": 10, \"valorRecebido\": 99}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pagamentos[0].forma").value("PIX"))
                .andExpect(jsonPath("$.pagamentos[0].valorRecebido").doesNotExist())
                .andExpect(jsonPath("$.pagamentos[0].troco").doesNotExist());
    }

    @Test
    @DisplayName("valor recebido em dinheiro menor que o valor do pagamento: 422")
    void recebidoMenorQueValor() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        assertRegraNegocio(pagarEmDinheiro(vendaId, "20.00", "15.00"),
                "O valor recebido em dinheiro não pode ser menor que o valor do pagamento");
        assertThat(pagamentoVendaRepository.count()).isZero();
    }

    @Test
    @DisplayName("pagamento acima do valor restante: 422")
    void pagamentoAcimaDoRestante() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        assertRegraNegocio(pagar(vendaId, "PIX", "30.00"),
                "O pagamento de R$ 30.00 ultrapassa o valor restante da venda (R$ 25.90)");

        pagar(vendaId, "PIX", "20.00").andExpect(status().isCreated());
        assertRegraNegocio(pagar(vendaId, "CREDITO", "6.00"),
                "O pagamento de R$ 6.00 ultrapassa o valor restante da venda (R$ 5.90)");
        assertThat(pagamentoVendaRepository.count()).isEqualTo(1);
    }

    static Stream<Arguments> pagamentosInvalidos() {
        return Stream.of(
                Arguments.of("forma ausente", "{\"valor\": 10}", "forma: A forma de pagamento é obrigatória"),
                Arguments.of("valor ausente", "{\"forma\": \"PIX\"}", "valor: O valor é obrigatório"),
                Arguments.of("valor zero", "{\"forma\": \"PIX\", \"valor\": 0}", "valor: O valor deve ser maior que zero"),
                Arguments.of("valor negativo", "{\"forma\": \"PIX\", \"valor\": -1}", "valor: O valor deve ser maior que zero"),
                Arguments.of("3 casas decimais", "{\"forma\": \"PIX\", \"valor\": 1.005}", "valor: Use no máximo 2 casas decimais")
        );
    }

    @ParameterizedTest(name = "{0} -> 400 e nada é gravado")
    @MethodSource("pagamentosInvalidos")
    void pagamentoInvalido(String cenario, String corpo, String campoEsperado) throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        assertErroValidacao(post("/vendas/%d/pagamentos".formatted(vendaId), corpo), campoEsperado);
        assertThat(pagamentoVendaRepository.count()).isZero();
    }

    @Test
    @DisplayName("o cliente pode pagar antes de a cozinha terminar: a comanda segue em preparo")
    void pagarAntesDaCozinha() throws Exception {
        Long comandaId = criarComanda(mesa, 1);
        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
        Long vendaId = vendaAbertaDa(mesa);

        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());

        get("/pedido/" + comandaId).andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    @DisplayName("remover pagamento da venda aberta devolve o valor ao restante")
    void removerPagamento() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", "10.00").andExpect(status().isCreated());
        Long pagamentoId = pagamentoVendaRepository.findByVendaIdOrderByIdAsc(vendaId).get(0).getId();

        delete("/vendas/%d/pagamentos/%d".formatted(vendaId, pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorPago").value(0.0))
                .andExpect(jsonPath("$.valorRestante").value(25.9))
                .andExpect(jsonPath("$.pagamentos.length()").value(0));
    }

    @Test
    @DisplayName("remover pagamento inexistente ou de outra venda: 404")
    void removerPagamentoInexistente() throws Exception {
        Long vendaA = vendaDaNovaComanda(mesa, 1);
        Long vendaB = vendaDaNovaComanda(criarMesa(2), 1);
        pagar(vendaA, "PIX", "10.00").andExpect(status().isCreated());
        Long pagamentoDeA = pagamentoVendaRepository.findByVendaIdOrderByIdAsc(vendaA).get(0).getId();

        assertNaoEncontrado(delete("/vendas/%d/pagamentos/%d".formatted(vendaB, pagamentoDeA)),
                "Pagamento não encontrado: " + pagamentoDeA);
        assertNaoEncontrado(delete("/vendas/%d/pagamentos/%d".formatted(vendaA, ID_INEXISTENTE)),
                "Pagamento não encontrado: " + ID_INEXISTENTE);
        assertThat(pagamentoVendaRepository.count()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ finalização

    @Test
    @DisplayName("finalizar sem caixa aberto: 422 e a venda continua aberta, com a mesa ocupada")
    void finalizarSemCaixa() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());

        assertRegraNegocio(finalizar(vendaId), "Abra o caixa antes de finalizar a venda");

        get("/vendas/" + vendaId).andExpect(jsonPath("$.status").value("ABERTA"));
        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(true);
    }

    @Test
    @DisplayName("finalizar com pagamentos diferentes do total: 422")
    void finalizarComPagamentoIncompleto() throws Exception {
        abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        assertRegraNegocio(finalizar(vendaId), "O valor pago (R$ 0.00) difere do total da venda (R$ 25.90)");

        pagar(vendaId, "PIX", "10.00").andExpect(status().isCreated());
        assertRegraNegocio(finalizar(vendaId), "O valor pago (R$ 10.00) difere do total da venda (R$ 25.90)");
        get("/vendas/" + vendaId).andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    @DisplayName("pagamento que excede o total depois de cancelar uma comanda bloqueia a finalização")
    void finalizarComPagamentoAcimaDoTotal() throws Exception {
        abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        Long segunda = criarComanda(mesa, 1);
        pagar(vendaId, "PIX", "51.80").andExpect(status().isCreated());
        mudarStatus(segunda, "CANCELADO").andExpect(status().isOk());

        assertRegraNegocio(finalizar(vendaId), "O valor pago (R$ 51.80) difere do total da venda (R$ 25.90)");
    }

    @Test
    @DisplayName("finalizar: libera a mesa, grava total, caixa e usuário, e lança só o dinheiro (sem troco) no caixa")
    void finalizarComDinheiroEPix() throws Exception {
        Long caixaId = abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 2);   // total 51,80
        pagarEmDinheiro(vendaId, "30.00", "50.00").andExpect(status().isCreated());   // troco de 20
        pagar(vendaId, "PIX", "21.80").andExpect(status().isCreated());

        finalizar(vendaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.valorTotal").value(51.8))
                .andExpect(jsonPath("$.valorPago").value(51.8))
                .andExpect(jsonPath("$.valorRestante").value(0.0))
                .andExpect(jsonPath("$.caixaId").value(caixaId))
                .andExpect(jsonPath("$.usuarioFechamento").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.dataFechamento").isNotEmpty());

        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(false);

        List<MovimentacaoCaixa> entradas = entradasDeVenda(caixaId);
        assertThat(entradas).hasSize(1);
        assertThat(entradas.get(0).getValor()).isEqualByComparingTo("30.00");
        assertThat(entradas.get(0).getVendaId()).isEqualTo(vendaId);
        assertThat(entradas.get(0).getUsuario()).isEqualTo(USUARIO_PADRAO);
        assertThat(entradas.get(0).getDescricao()).isEqualTo("Venda #" + vendaId);
        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(130.0));
    }

    @Test
    @DisplayName("venda paga só com Pix: exige caixa aberto mas não gera lançamento no caixa")
    void finalizarSoComPix() throws Exception {
        Long caixaId = abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());

        finalizar(vendaId).andExpect(status().isOk()).andExpect(jsonPath("$.caixaId").value(caixaId));

        assertThat(entradasDeVenda(caixaId)).isEmpty();
        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(100.0));
    }

    @Test
    @DisplayName("funcionário também finaliza a venda (o responsável fica registrado)")
    void funcionarioFinaliza() throws Exception {
        abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());

        postComo(tokenDe("func.teste", "FUNCIONARIO"), "/vendas/%d/finalizar".formatted(vendaId), "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioFechamento").value("func.teste"));
    }

    @Test
    @DisplayName("finalizar com a cozinha ainda preparando: libera a mesa e a cozinha continua trabalhando")
    void finalizarComCozinhaPreparando() throws Exception {
        abrirCaixa("100");
        Long comandaId = criarComanda(mesa, 1);
        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
        Long vendaId = vendaAbertaDa(mesa);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());

        finalizar(vendaId).andExpect(status().isOk());

        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(false);
        get("/pedido/" + comandaId).andExpect(jsonPath("$.status").value("EM_PREPARO"));
        mudarStatus(comandaId, "FINALIZADO").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINALIZADO"));
    }

    @Test
    @DisplayName("finalizar a mesma venda duas vezes: 422")
    void finalizarDuasVezes() throws Exception {
        abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        finalizar(vendaId).andExpect(status().isOk());

        assertRegraNegocio(finalizar(vendaId), "A venda não está aberta");
        assertThat(entradasDeVenda(caixaRepository.findAll().get(0).getId())).isEmpty();
    }

    @Test
    @DisplayName("venda sem comandas não pode ser finalizada")
    void finalizarSemComandas() throws Exception {
        abrirCaixa("100");
        Long vendaId = idDaResposta(post("/vendas", "{\"nomeCliente\": \"Retirada\"}").andExpect(status().isCreated()));

        assertRegraNegocio(finalizar(vendaId), "A venda não tem comandas para finalizar");
    }

    @Test
    @DisplayName("depois de finalizada, uma nova comanda na mesa abre outra venda")
    void novaComandaAposFinalizar() throws Exception {
        abrirCaixa("100");
        Long primeira = vendaDaNovaComanda(mesa, 1);
        pagar(primeira, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        finalizar(primeira).andExpect(status().isOk());

        Long segunda = vendaDaNovaComanda(mesa, 1);

        assertThat(segunda).isNotEqualTo(primeira);
        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(true);
        assertThat(vendaRepository.count()).isEqualTo(2);
    }

    // ------------------------------------------------------------------ venda finalizada fica travada

    @Test
    @DisplayName("venda finalizada não aceita pagamento, remoção de pagamento nem edição")
    void vendaFinalizadaTravada() throws Exception {
        abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        Long pagamentoId = pagamentoVendaRepository.findByVendaIdOrderByIdAsc(vendaId).get(0).getId();
        finalizar(vendaId).andExpect(status().isOk());

        assertRegraNegocio(pagar(vendaId, "PIX", "1.00"), "A venda não está aberta");
        assertRegraNegocio(delete("/vendas/%d/pagamentos/%d".formatted(vendaId, pagamentoId)), "A venda não está aberta");
        assertRegraNegocio(patch("/vendas/" + vendaId, "{\"nomeCliente\": \"Outro\"}"), "A venda não está aberta");
    }

    @Test
    @DisplayName("comanda de venda finalizada não aceita itens novos, alteração nem exclusão")
    void comandaDeVendaFinalizadaTravada() throws Exception {
        abrirCaixa("100");
        Long comandaId = criarComanda(mesa, 1);
        Long vendaId = vendaAbertaDa(mesa);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        finalizar(vendaId).andExpect(status().isOk());
        String mensagem = "A venda desta comanda já foi finalizada; não é possível alterar a comanda";

        assertRegraNegocio(post("/item-pedido/pedido/" + comandaId,
                "{\"produtoId\": %d, \"quantidade\": 1}".formatted(produto.getId())), mensagem);
        assertRegraNegocio(put("/pedido/" + comandaId, corpoComanda(mesa, 3)), mensagem);
        assertRegraNegocio(delete("/pedido/" + comandaId), mensagem);
        assertThat(pedidoRepository.existsById(comandaId)).isTrue();
    }

    @Test
    @DisplayName("comanda de venda finalizada não pode ser cancelada, mas o preparo pode avançar")
    void comandaDeVendaFinalizadaNaoCancela() throws Exception {
        abrirCaixa("100");
        Long comandaId = criarComanda(mesa, 1);
        Long vendaId = vendaAbertaDa(mesa);
        pagar(vendaId, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        finalizar(vendaId).andExpect(status().isOk());

        assertRegraNegocio(mudarStatus(comandaId, "CANCELADO"),
                "A venda desta comanda já foi finalizada; a comanda não pode ser cancelada");
        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ integração com o relatório do caixa

    @Test
    @DisplayName("relatório de fechamento do caixa traz o total de vendas em dinheiro e o lançamento com a venda de origem")
    void relatorioDoCaixaComVendas() throws Exception {
        Long caixaId = abrirCaixa("100");
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        pagarEmDinheiro(vendaId, PRECO_TEXTO, "30.00").andExpect(status().isCreated());
        finalizar(vendaId).andExpect(status().isOk());

        post("/caixa/%d/fechar".formatted(caixaId), jsonFechamento("125.90", "")).andExpect(status().isOk())
                .andExpect(jsonPath("$.valorEsperado").value(125.9))
                .andExpect(jsonPath("$.diferenca").value(0.0));

        get("/caixa/%d/relatorio-fechamento".formatted(caixaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVendas").value(25.9))
                .andExpect(jsonPath("$.totalSuprimentos").value(0.0))
                .andExpect(jsonPath("$.movimentacoes[1].tipo").value("VENDA"))
                .andExpect(jsonPath("$.movimentacoes[1].vendaId").value(vendaId));
    }

    @Test
    @DisplayName("lançamento do tipo VENDA não pode ser criado pela API do caixa")
    void vendaNaoEhLancavelPeloCaixa() throws Exception {
        Long caixaId = abrirCaixa("100");

        assertRegraNegocio(post("/caixa/%d/movimentacoes".formatted(caixaId), jsonMovimentacao("VENDA", "10", "Venda")),
                "As vendas entram no caixa ao finalizar a venda");
    }
}
