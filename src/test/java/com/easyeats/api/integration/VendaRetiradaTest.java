package com.easyeats.api.integration;

import com.easyeats.api.entity.Pedido;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.support.VendaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Atendimento por retirada (sem mesa) e delivery (ainda indisponível). A retirada abre uma venda própria,
 * identificada pelo nome do cliente, e usa os mesmos pagamentos, caixa e cancelamento da venda de mesa.
 */
@DisplayName("Venda: retirada e delivery")
class VendaRetiradaTest extends VendaTestBase {

    // ------------------------------------------------------------------ retirada

    @Test
    @DisplayName("comanda de retirada: 201, sem mesa, com observação, e abre uma venda RETIRADA com o nome do cliente")
    void comandaDeRetirada() throws Exception {
        var resposta = lancarRetirada("Maria", 2)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mesaId").doesNotExist())
                .andExpect(jsonPath("$.vendaId").isNumber())
                .andExpect(jsonPath("$.tipoVenda").value("RETIRADA"))
                .andExpect(jsonPath("$.cliente").value("Maria"))
                .andExpect(jsonPath("$.observacao").value("Sem cebola"))
                .andExpect(jsonPath("$.valorTotal").value(51.8))
                .andExpect(jsonPath("$.status").value("ABERTO"));
        Long vendaId = vendaIdDaResposta(resposta);

        get("/vendas/" + vendaId)
                .andExpect(jsonPath("$.tipo").value("RETIRADA"))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.mesaId").doesNotExist())
                .andExpect(jsonPath("$.nomeCliente").value("Maria"))
                .andExpect(jsonPath("$.usuarioAbertura").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.comandas.length()").value(1));
    }

    @Test
    @DisplayName("retirada não ocupa nenhuma mesa")
    void retiradaNaoOcupaMesa() throws Exception {
        lancarRetirada("Maria", 1).andExpect(status().isCreated());

        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(false);
    }

    @Test
    @DisplayName("retirada sem nome do cliente: 422 e nada é gravado")
    void retiradaSemNome() throws Exception {
        assertRegraNegocio(post("/pedido", "{\"tipoVenda\": \"RETIRADA\", \"itens\": [{\"produtoId\": %d, \"quantidade\": 1}]}"
                        .formatted(produto.getId())),
                "Informe o nome do cliente da retirada");

        assertThat(vendaRepository.count()).isZero();
        assertThat(pedidoRepository.count()).isZero();
    }

    @Test
    @DisplayName("duas retiradas simultâneas são vendas separadas")
    void retiradasSeparadas() throws Exception {
        Long maria = vendaDaNovaRetirada("Maria", 1);
        Long joao = vendaDaNovaRetirada("João", 1);

        assertThat(maria).isNotEqualTo(joao);
        assertThat(vendaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("nova comanda na mesma retirada: informa o vendaId e soma no total")
    void segundaComandaNaRetirada() throws Exception {
        Long vendaId = vendaDaNovaRetirada("Maria", 1);

        post("/pedido", ("{\"tipoVenda\": \"RETIRADA\", \"vendaId\": %d, \"itens\": "
                + "[{\"produtoId\": %d, \"quantidade\": 2}]}").formatted(vendaId, produto.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendaId").value(vendaId));

        get("/vendas/" + vendaId)
                .andExpect(jsonPath("$.comandas.length()").value(2))
                .andExpect(jsonPath("$.valorTotal").value(77.7));
        assertThat(vendaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("vendaId de venda que não é retirada ou que já foi encerrada é recusado")
    void vendaIdInvalido() throws Exception {
        Long vendaDeMesa = vendaDaNovaComanda(mesa, 1);
        abrirCaixa("100");
        Long retirada = vendaDaNovaRetirada("Maria", 1);
        pagar(retirada, "PIX", PRECO_TEXTO).andExpect(status().isCreated());
        finalizar(retirada).andExpect(status().isOk());
        String corpo = "{\"tipoVenda\": \"RETIRADA\", \"vendaId\": %d, \"itens\": [{\"produtoId\": %d, \"quantidade\": 1}]}";

        assertRegraNegocio(post("/pedido", corpo.formatted(vendaDeMesa, produto.getId())),
                "A venda informada não é uma retirada");
        assertRegraNegocio(post("/pedido", corpo.formatted(retirada, produto.getId())), "A venda não está aberta");
    }

    @Test
    @DisplayName("retirada paga e finalizada: dinheiro entra no caixa e nenhuma mesa é afetada")
    void retiradaPagaEFinalizada() throws Exception {
        Long caixaId = abrirCaixa("100");
        Long vendaId = vendaDaNovaRetirada("Maria", 1);
        pagarEmDinheiro(vendaId, PRECO_TEXTO, "50.00").andExpect(status().isCreated());

        finalizar(vendaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.tipo").value("RETIRADA"))
                .andExpect(jsonPath("$.caixaId").value(caixaId));

        get("/caixa/atual").andExpect(jsonPath("$.saldoAtual").value(125.9));
    }

    @Test
    @DisplayName("retirada pode ser cancelada pela gestão, com devolução opcional do estoque")
    void retiradaCancelada() throws Exception {
        Long comandaId = idDaResposta(lancarRetirada("Maria", 2).andExpect(status().isCreated()));
        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
        Long vendaId = pedidoRepository.findById(comandaId).orElseThrow().getVenda().getId();

        cancelarDevolvendoEstoque(vendaId, "Cliente não veio buscar", SENHA_TESTE).andExpect(status().isOk());

        assertThat(estoqueDoPao()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("comanda de retirada percorre o preparo e pode ser editada (não tem mesa para validar)")
    void comandaDeRetiradaPercorrePreparoEEdita() throws Exception {
        Long comandaId = idDaResposta(lancarRetirada("Maria", 1).andExpect(status().isCreated()));

        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
        mudarStatus(comandaId, "FINALIZADO").andExpect(status().isOk());
        put("/pedido/" + comandaId, corpoRetirada("Maria Souza", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Maria Souza"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3));

        Pedido salvo = pedidoRepository.findById(comandaId).orElseThrow();
        assertThat(salvo.getMesa()).isNull();
        // o total da venda vem dos itens, então reflete a edição
        get("/vendas/" + salvo.getVenda().getId()).andExpect(jsonPath("$.valorTotal").value(77.7));
    }

    @Test
    @DisplayName("observação da comanda é opcional, aparece na consulta e respeita o limite de tamanho")
    void observacaoDaComanda() throws Exception {
        Long semObservacao = criarComanda(mesa, 1);
        get("/pedido/" + semObservacao).andExpect(jsonPath("$.observacao").doesNotExist());

        Long comandaId = idDaResposta(lancarRetirada("Maria", 1).andExpect(status().isCreated()));
        get("/pedido/" + comandaId).andExpect(jsonPath("$.observacao").value("Sem cebola"));

        String longa = "x".repeat(256);
        assertErroValidacao(post("/pedido", ("{\"tipoVenda\": \"RETIRADA\", \"cliente\": \"Maria\", \"observacao\": \"%s\", "
                + "\"itens\": [{\"produtoId\": %d, \"quantidade\": 1}]}").formatted(longa, produto.getId())),
                "observacao: A observação deve ter no máximo 255 caracteres");
    }

    // ------------------------------------------------------------------ vendas abertas

    @Test
    @DisplayName("vendas abertas: mesas e retiradas, da mais antiga para a mais nova, com total parcial e para qualquer usuário")
    void vendasAbertas() throws Exception {
        criarComanda(mesa, 1);
        vendaDaNovaRetirada("Maria", 2);

        getComo(tokenDe("func.teste", "FUNCIONARIO"), "/vendas/abertas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("MESA"))
                .andExpect(jsonPath("$[0].numeroMesa").value(1))
                .andExpect(jsonPath("$[0].valorTotal").value(25.9))
                .andExpect(jsonPath("$[1].tipo").value("RETIRADA"))
                .andExpect(jsonPath("$[1].nomeCliente").value("Maria"))
                .andExpect(jsonPath("$[1].valorTotal").value(51.8));
    }

    @Test
    @DisplayName("vendas abertas: venda encerrada sai da lista")
    void vendasAbertasSemEncerradas() throws Exception {
        Long vendaId = vendaDaNovaRetirada("Maria", 1);
        cancelar(vendaId, "Desistiu").andExpect(status().isOk());

        get("/vendas/abertas").andExpect(jsonPath("$.length()").value(0));
    }

    // ------------------------------------------------------------------ delivery e regras da abertura explícita

    @Test
    @DisplayName("delivery ainda não está disponível: 422 na comanda e na abertura de venda")
    void deliveryIndisponivel() throws Exception {
        assertRegraNegocio(post("/pedido", ("{\"tipoVenda\": \"DELIVERY\", \"cliente\": \"Maria\", "
                + "\"itens\": [{\"produtoId\": %d, \"quantidade\": 1}]}").formatted(produto.getId())),
                "O delivery estará disponível em breve");
        assertRegraNegocio(post("/vendas", "{\"tipo\": \"DELIVERY\", \"nomeCliente\": \"Maria\"}"),
                "O delivery estará disponível em breve");

        assertThat(vendaRepository.count()).isZero();
        assertThat(pedidoRepository.count()).isZero();
    }

    @Test
    @DisplayName("abertura explícita de retirada: exige nome e não aceita mesa")
    void aberturaExplicitaDeRetirada() throws Exception {
        post("/vendas", "{\"tipo\": \"RETIRADA\", \"nomeCliente\": \"Maria\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RETIRADA"));

        assertRegraNegocio(post("/vendas", "{\"tipo\": \"RETIRADA\"}"), "Informe o nome do cliente da retirada");
        assertRegraNegocio(post("/vendas", "{\"tipo\": \"RETIRADA\", \"nomeCliente\": \"Maria\", \"mesaId\": %d}"
                .formatted(mesa.getId())), "A retirada não usa mesa");
    }

    @Test
    @DisplayName("abertura explícita de venda de mesa exige a mesa")
    void aberturaExplicitaDeMesaExigeMesa() throws Exception {
        assertRegraNegocio(post("/vendas", "{\"tipo\": \"MESA\"}"), "Informe a mesa da venda");
    }

    @Test
    @DisplayName("tipo de venda padrão: MESA quando há mesa, RETIRADA quando não há")
    void tipoPadrao() throws Exception {
        assertThat(vendaDaNovaComanda(mesa, 1)).isNotNull();
        get("/vendas/mesa/" + mesa.getId() + "/aberta").andExpect(jsonPath("$.tipo").value(TipoVendaEnum.MESA.name()));
        post("/vendas", "{\"nomeCliente\": \"Pedro\"}").andExpect(jsonPath("$.tipo").value("RETIRADA"));
    }
}
