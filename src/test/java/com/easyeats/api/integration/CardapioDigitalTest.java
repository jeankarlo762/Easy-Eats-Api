package com.easyeats.api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.OrigemVendaEnum;
import com.easyeats.api.entity.StatusPedidoEnum;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;
import com.easyeats.api.integracao.whatsapp.WhatsAppService;
import com.easyeats.api.support.VendaTestBase;

/**
 * Cardápio digital (QR Code): rotas públicas, pedido de delivery/retirada feito pelo cliente e aviso por WhatsApp.
 * O envio real é substituído por um mock do WhatsAppService.
 */
@DisplayName("Cardápio digital")
@TestPropertySource(properties = {
        "integracao.whatsapp.numero-loja=5511988887777",
        "cardapio.limite-pedidos-por-ip=1000"
})
class CardapioDigitalTest extends VendaTestBase {

    @MockitoBean
    private WhatsAppService whatsApp;

    private String pedidoDelivery(String forma, String troco) {
        return """
                {"nome": "Maria Souza", "telefone": "(11) 97777-6666", "tipoEntrega": "DELIVERY",
                 "endereco": {"rua": "Rua das Flores", "numero": "123", "bairro": "Centro", "cidade": "São Paulo"},
                 "formaPagamento": "%s", "trocoPara": %s, "observacao": "Sem cebola",
                 "itens": [{"produtoId": %d, "quantidade": 2}]}
                """.formatted(forma, troco, produto.getId());
    }

    @Test
    @DisplayName("cardápio é público e lista só produtos ativos com preço")
    void cardapioPublico() throws Exception {
        criarProdutoInativo("Antigo", produto.getCategoria(), new BigDecimal("9.00"));

        getSemToken("/publico/cardapio")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(1))
                .andExpect(jsonPath("$.categorias[0].nome").value("Lanches"))
                .andExpect(jsonPath("$.categorias[0].produtos.length()").value(1))
                .andExpect(jsonPath("$.categorias[0].produtos[0].nome").value("X-Burger"))
                .andExpect(jsonPath("$.categorias[0].produtos[0].preco").value(25.90))
                .andExpect(jsonPath("$.formasPagamento.length()").value(4));
    }

    @Test
    @DisplayName("pedido de delivery sem login: 201, cria venda DELIVERY do cardápio e comanda ABERTA, e avisa no WhatsApp")
    void pedidoDelivery() throws Exception {
        postSemToken("/publico/cardapio/pedidos", pedidoDelivery("DINHEIRO", "100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroPedido").isNumber())
                .andExpect(jsonPath("$.tipoEntrega").value("DELIVERY"))
                .andExpect(jsonPath("$.total").value(51.80));

        Venda venda = vendaRepository.findAll().getFirst();
        assertThat(venda.getTipo()).isEqualTo(TipoVendaEnum.DELIVERY);
        assertThat(venda.getOrigem()).isEqualTo(OrigemVendaEnum.CARDAPIO_DIGITAL);
        assertThat(venda.getNomeCliente()).isEqualTo("Maria Souza");
        assertThat(venda.getTelefoneCliente()).isEqualTo("11977776666");
        assertThat(venda.getEnderecoEntrega()).isEqualTo("Rua das Flores, 123, Centro, São Paulo");
        assertThat(venda.getFormaPagamentoPrevista()).isEqualTo(FormaPagamentoEnum.DINHEIRO);
        assertThat(venda.getTrocoPara()).isEqualByComparingTo("100");
        assertThat(venda.getMesa()).isNull();
        assertThat(pedidoRepository.findAll()).singleElement()
                .satisfies(p -> assertThat(p.getStatus()).isEqualTo(StatusPedidoEnum.ABERTO));

        // aparece nas vendas abertas do sistema
        get("/vendas/abertas")
                .andExpect(jsonPath("$[0].tipo").value("DELIVERY"))
                .andExpect(jsonPath("$[0].origem").value("CARDAPIO_DIGITAL"))
                .andExpect(jsonPath("$[0].enderecoEntrega").value("Rua das Flores, 123, Centro, São Paulo"));

        verify(whatsApp).enviar(eq("11977776666"), argThat(m -> m.contains("Recebemos seu pedido")
                && m.contains("2x X-Burger") && m.contains("troco para")));
        verify(whatsApp).enviar(eq("5511988887777"), argThat(m -> m.contains("Novo pedido")));
    }

    @Test
    @DisplayName("retirada não exige endereço")
    void pedidoRetirada() throws Exception {
        postSemToken("/publico/cardapio/pedidos", """
                {"nome": "João", "telefone": "11977776666", "tipoEntrega": "RETIRADA", "formaPagamento": "PIX",
                 "itens": [{"produtoId": %d, "quantidade": 1}]}
                """.formatted(produto.getId()))
                .andExpect(status().isCreated());

        Venda venda = vendaRepository.findAll().getFirst();
        assertThat(venda.getTipo()).isEqualTo(TipoVendaEnum.RETIRADA);
        assertThat(venda.getEnderecoEntrega()).isNull();
    }

    @Test
    @DisplayName("delivery sem endereço: 422 e nada é gravado nem enviado")
    void deliverySemEndereco() throws Exception {
        assertRegraNegocio(postSemToken("/publico/cardapio/pedidos", """
                {"nome": "Maria", "telefone": "11977776666", "formaPagamento": "PIX",
                 "itens": [{"produtoId": %d, "quantidade": 1}]}
                """.formatted(produto.getId())), "Informe rua, número e bairro para a entrega");

        assertThat(vendaRepository.count()).isZero();
        verify(whatsApp, never()).enviar(anyString(), anyString());
    }

    @Test
    @DisplayName("telefone inválido e troco menor que o total: 422")
    void validacoes() throws Exception {
        assertRegraNegocio(postSemToken("/publico/cardapio/pedidos", pedidoDelivery("PIX", "null")
                        .replace("(11) 97777-6666", "1234")),
                "Informe um telefone válido com DDD");
        assertRegraNegocio(postSemToken("/publico/cardapio/pedidos", pedidoDelivery("DINHEIRO", "10")),
                "O troco deve ser para um valor maior ou igual ao total do pedido");
        assertThat(vendaRepository.count()).isZero();
    }

    @Test
    @DisplayName("pedido sem itens ou sem forma de pagamento: 400")
    void camposObrigatorios() throws Exception {
        assertErroValidacao(postSemToken("/publico/cardapio/pedidos",
                "{\"nome\": \"Maria\", \"telefone\": \"11977776666\", \"itens\": []}"),
                "itens: Adicione ao menos um item", "formaPagamento: Escolha a forma de pagamento");
    }
}
