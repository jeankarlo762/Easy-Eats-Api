package com.easyeats.api.support;

import com.easyeats.api.entity.Categoria;
import com.easyeats.api.entity.Ingrediente;
import com.easyeats.api.entity.Mesa;
import com.easyeats.api.entity.Produto;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Massa e helpers dos testes de Venda: uma mesa, o produto X-Burger (R$ 25,90, consome 1 pão) e
 * estoque de 100 pães.
 */
public abstract class VendaTestBase extends CaixaTestBase {

    protected static final String PRECO_TEXTO = "25.90";

    protected Mesa mesa;
    protected Produto produto;
    protected Ingrediente pao;

    @BeforeEach
    void criarMassaDaVenda() {
        Categoria categoria = criarCategoria("Lanches");
        mesa = criarMesa(1);
        pao = criarIngrediente("Pão");
        criarEstoque(pao, 100);
        produto = criarProduto("X-Burger", categoria, new BigDecimal(PRECO_TEXTO));
        vincular(produto, pao, 1);
    }

    // ------------------------------------------------------------------ comandas

    protected String corpoComanda(Mesa daMesa, int quantidade) {
        return "{\"mesaId\": %d, \"cliente\": \"João\", \"itens\": [{\"produtoId\": %d, \"quantidade\": %d}]}"
                .formatted(daMesa.getId(), produto.getId(), quantidade);
    }

    protected ResultActions lancarComanda(Mesa daMesa, int quantidade) throws Exception {
        return post("/pedido", corpoComanda(daMesa, quantidade));
    }

    /** Cria a comanda e devolve o id do pedido. */
    protected Long criarComanda(Mesa daMesa, int quantidade) throws Exception {
        return idDaResposta(lancarComanda(daMesa, quantidade).andExpect(status().isCreated()));
    }

    /** Corpo de uma comanda de retirada (sem mesa), com observação. */
    protected String corpoRetirada(String cliente, int quantidade) {
        return ("{\"tipoVenda\": \"RETIRADA\", \"cliente\": \"%s\", \"observacao\": \"Sem cebola\", "
                + "\"itens\": [{\"produtoId\": %d, \"quantidade\": %d}]}").formatted(cliente, produto.getId(), quantidade);
    }

    protected ResultActions lancarRetirada(String cliente, int quantidade) throws Exception {
        return post("/pedido", corpoRetirada(cliente, quantidade));
    }

    /** Lança uma retirada (nova venda) e devolve o id da venda. */
    protected Long vendaDaNovaRetirada(String cliente, int quantidade) throws Exception {
        return vendaIdDaResposta(lancarRetirada(cliente, quantidade).andExpect(status().isCreated()));
    }
    /** Cria a comanda e devolve o id da venda aberta por ela (ou a que ela integrou). */
    protected Long vendaDaNovaComanda(Mesa daMesa, int quantidade) throws Exception {
        return vendaIdDaResposta(lancarComanda(daMesa, quantidade).andExpect(status().isCreated()));
    }

    protected Long vendaIdDaResposta(ResultActions resposta) throws Exception {
        Number id = JsonPath.read(corpo(resposta), "$.vendaId");
        return id.longValue();
    }

    /** Id da venda aberta da mesa. */
    protected Long vendaAbertaDa(Mesa daMesa) throws Exception {
        Number id = JsonPath.read(corpo(get("/vendas/mesa/" + daMesa.getId() + "/aberta")), "$.id");
        return id.longValue();
    }

    protected ResultActions mudarStatus(Long pedidoId, String novoStatus) throws Exception {
        return patch("/pedido/" + pedidoId + "/status/" + novoStatus);
    }

    // ------------------------------------------------------------------ pagamento e finalização

    protected ResultActions pagar(Long vendaId, String forma, String valor) throws Exception {
        return post("/vendas/%d/pagamentos".formatted(vendaId),
                "{\"forma\": \"%s\", \"valor\": %s}".formatted(forma, valor));
    }

    protected ResultActions pagarEmDinheiro(Long vendaId, String valor, String recebido) throws Exception {
        return post("/vendas/%d/pagamentos".formatted(vendaId),
                "{\"forma\": \"DINHEIRO\", \"valor\": %s, \"valorRecebido\": %s}".formatted(valor, recebido));
    }

    protected ResultActions finalizar(Long vendaId) throws Exception {
        return post("/vendas/%d/finalizar".formatted(vendaId), "{}");
    }

    /** Cancela pedindo a devolução do estoque, confirmada com a senha do gerente. */
    protected ResultActions cancelarDevolvendoEstoque(Long vendaId, String motivo, String senha) throws Exception {
        return post("/vendas/%d/cancelar".formatted(vendaId),
                "{\"motivo\": \"%s\", \"devolverEstoque\": true, \"senhaGerente\": \"%s\"}".formatted(motivo, senha));
    }

    protected ResultActions cancelar(Long vendaId, String motivo) throws Exception {
        return post("/vendas/%d/cancelar".formatted(vendaId), "{\"motivo\": \"%s\"}".formatted(motivo));
    }

    // ------------------------------------------------------------------ consultas

    /** Linha da mesa em GET /vendas/ocupacao-mesas. */
    protected Map<String, Object> ocupacaoDe(Mesa daMesa) throws Exception {
        List<Map<String, Object>> linhas = JsonPath.read(corpo(get("/vendas/ocupacao-mesas")),
                "$[?(@.mesaId == %d)]".formatted(daMesa.getId()));
        return linhas.get(0);
    }

    protected BigDecimal estoqueDoPao() {
        return estoqueRepository.findByIngredienteId(pao.getId()).orElseThrow().getQtdAtual();
    }
}