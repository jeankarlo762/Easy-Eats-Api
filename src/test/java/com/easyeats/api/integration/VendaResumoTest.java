package com.easyeats.api.integration;

import com.easyeats.api.support.VendaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Faturamento do período (Administrador e Gerente): vendas finalizadas pela data de fechamento. */
@DisplayName("Venda: resumo do faturamento por período")
class VendaResumoTest extends VendaTestBase {

    private String hoje() {
        return LocalDate.now().toString();
    }

    private void venderEmDinheiro(String valor, int quantidade) throws Exception {
        Long vendaId = vendaDaNovaRetirada("Cliente " + quantidade, quantidade);
        pagarEmDinheiro(vendaId, valor, valor).andExpect(status().isCreated());
        finalizar(vendaId).andExpect(status().isOk());
    }

    @Test
    @DisplayName("resumo: total, quantidade, ticket médio, canceladas, por forma de pagamento e por dia")
    void resumoCompleto() throws Exception {
        abrirCaixa("100");
        venderEmDinheiro("25.90", 1);                                   // dinheiro 25,90
        Long segunda = vendaDaNovaRetirada("Cliente Pix", 2);           // pix 51,80
        pagar(segunda, "PIX", "51.80").andExpect(status().isCreated());
        finalizar(segunda).andExpect(status().isOk());
        cancelar(vendaDaNovaRetirada("Cliente desistiu", 1), "Desistiu").andExpect(status().isOk());

        get("/vendas/resumo?inicio=%s&fim=%s".formatted(hoje(), hoje()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFaturado").value(77.7))
                .andExpect(jsonPath("$.quantidadeVendas").value(2))
                .andExpect(jsonPath("$.ticketMedio").value(38.85))
                .andExpect(jsonPath("$.quantidadeCanceladas").value(1))
                .andExpect(jsonPath("$.porFormaPagamento.length()").value(2))
                .andExpect(jsonPath("$.porFormaPagamento[0].forma").value("DINHEIRO"))
                .andExpect(jsonPath("$.porFormaPagamento[0].total").value(25.9))
                .andExpect(jsonPath("$.porFormaPagamento[1].forma").value("PIX"))
                .andExpect(jsonPath("$.porFormaPagamento[1].total").value(51.8))
                .andExpect(jsonPath("$.porDia.length()").value(1))
                .andExpect(jsonPath("$.porDia[0].dia").value(hoje()))
                .andExpect(jsonPath("$.porDia[0].quantidade").value(2))
                .andExpect(jsonPath("$.porDia[0].total").value(77.7));
    }

    @Test
    @DisplayName("sem datas, o resumo considera hoje")
    void semDatasConsideraHoje() throws Exception {
        abrirCaixa("100");
        venderEmDinheiro("25.90", 1);

        get("/vendas/resumo")
                .andExpect(jsonPath("$.totalFaturado").value(25.9))
                .andExpect(jsonPath("$.quantidadeVendas").value(1));
    }

    @Test
    @DisplayName("período sem vendas devolve tudo zerado")
    void periodoSemVendas() throws Exception {
        abrirCaixa("100");
        venderEmDinheiro("25.90", 1);

        get("/vendas/resumo?inicio=2020-01-01&fim=2020-01-31")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFaturado").value(0.0))
                .andExpect(jsonPath("$.quantidadeVendas").value(0))
                .andExpect(jsonPath("$.ticketMedio").value(0.0))
                .andExpect(jsonPath("$.quantidadeCanceladas").value(0))
                .andExpect(jsonPath("$.porFormaPagamento.length()").value(0))
                .andExpect(jsonPath("$.porDia.length()").value(0));
    }

    @Test
    @DisplayName("venda aberta ainda não conta no faturamento")
    void vendaAbertaNaoConta() throws Exception {
        vendaDaNovaRetirada("Maria", 3);

        get("/vendas/resumo").andExpect(jsonPath("$.totalFaturado").value(0.0));
    }

    @Test
    @DisplayName("funcionário não acessa o resumo: 403; gerente acessa")
    void permissoes() throws Exception {
        getComo(tokenDe("func.teste", "FUNCIONARIO"), "/vendas/resumo")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida apenas para Administrador ou Gerente"));
        getComo(tokenDe("gerente.teste", "GERENTE"), "/vendas/resumo").andExpect(status().isOk());
    }
}
