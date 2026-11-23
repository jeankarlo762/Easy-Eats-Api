package com.easyeats.api.integration;

import com.easyeats.api.entity.Mesa;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;
import com.easyeats.api.support.VendaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A venda abre sozinha na primeira comanda da mesa e mantém a mesa ocupada até ser finalizada
 * ou cancelada, independentemente do status de preparo da cozinha.
 */
@DisplayName("Venda: abertura automática, comandas e ocupação da mesa")
class VendaComandaTest extends VendaTestBase {

    // ------------------------------------------------------------------ abertura automática

    @Test
    @DisplayName("primeira comanda da mesa abre a venda, com usuário do token e cliente da comanda")
    void primeiraComandaAbreVenda() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 2);

        get("/vendas/" + vendaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.mesaId").value(mesa.getId()))
                .andExpect(jsonPath("$.numeroMesa").value(1))
                .andExpect(jsonPath("$.nomeCliente").value("João"))
                .andExpect(jsonPath("$.usuarioAbertura").value(USUARIO_PADRAO))
                .andExpect(jsonPath("$.valorTotal").value(51.8))
                .andExpect(jsonPath("$.valorPago").value(0.0))
                .andExpect(jsonPath("$.valorRestante").value(51.8))
                .andExpect(jsonPath("$.comandas.length()").value(1))
                .andExpect(jsonPath("$.pagamentos.length()").value(0));
        assertThat(vendaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("novas comandas da mesma mesa entram na mesma venda e o total é a soma delas")
    void comandasDaMesmaMesaFicamNaMesmaVenda() throws Exception {
        Long primeira = vendaDaNovaComanda(mesa, 1);
        Long segunda = vendaDaNovaComanda(mesa, 2);

        assertThat(segunda).isEqualTo(primeira);
        assertThat(vendaRepository.count()).isEqualTo(1);
        get("/vendas/" + primeira)
                .andExpect(jsonPath("$.comandas.length()").value(2))
                .andExpect(jsonPath("$.valorTotal").value(77.7));
    }

    @Test
    @DisplayName("mesas diferentes têm vendas diferentes")
    void mesasDiferentesVendasDiferentes() throws Exception {
        Mesa outra = criarMesa(2);

        Long vendaMesa1 = vendaDaNovaComanda(mesa, 1);
        Long vendaMesa2 = vendaDaNovaComanda(outra, 1);

        assertThat(vendaMesa1).isNotEqualTo(vendaMesa2);
        assertThat(vendaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("comanda sem mesa continua barrada (422) e não abre venda")
    void comandaSemMesaNaoAbreVenda() throws Exception {
        post("/pedido", "{\"cliente\": \"João\", \"itens\": [{\"produtoId\": %d, \"quantidade\": 1}]}"
                .formatted(produto.getId())).andExpect(status().isUnprocessableEntity());

        assertThat(vendaRepository.count()).isZero();
    }

    // ------------------------------------------------------------------ ocupação da mesa

    @Test
    @DisplayName("mesa sem venda aberta está livre")
    void mesaSemVendaEstaLivre() throws Exception {
        Map<String, Object> linha = ocupacaoDe(mesa);

        assertThat(linha.get("ocupada")).isEqualTo(false);
        assertThat(linha.get("vendaId")).isNull();
        assertThat(((Number) linha.get("quantidadeComandas")).longValue()).isZero();
    }

    @Test
    @DisplayName("comanda pronta na cozinha (FINALIZADO) não libera a mesa: continua ocupada até a venda acabar")
    void comandaProntaNaoLiberaMesa() throws Exception {
        Long comandaId = criarComanda(mesa, 1);
        mudarStatus(comandaId, "EM_PREPARO").andExpect(status().isOk());
        mudarStatus(comandaId, "FINALIZADO").andExpect(status().isOk());

        Map<String, Object> linha = ocupacaoDe(mesa);

        assertThat(linha.get("ocupada")).isEqualTo(true);
        assertThat(linha.get("vendaId")).isNotNull();
    }

    @Test
    @DisplayName("ocupação traz venda, cliente, quantidade de comandas e valor parcial sem as canceladas")
    void ocupacaoComTotais() throws Exception {
        criarComanda(mesa, 1);
        Long cancelada = criarComanda(mesa, 4);
        criarComanda(mesa, 2);
        mudarStatus(cancelada, "CANCELADO").andExpect(status().isOk());

        Map<String, Object> linha = ocupacaoDe(mesa);

        assertThat(linha.get("ocupada")).isEqualTo(true);
        assertThat(linha.get("nomeCliente")).isEqualTo("João");
        assertThat(((Number) linha.get("quantidadeComandas")).longValue()).isEqualTo(2);
        assertThat(((Number) linha.get("valorParcial")).doubleValue()).isEqualTo(77.7);
    }

    @Test
    @DisplayName("a lista de ocupação traz todas as mesas ativas, em ordem de número, e ignora as inativas")
    void listaDeOcupacao() throws Exception {
        criarMesa(3);
        criarMesaInativa(2);
        criarComanda(mesa, 1);

        get("/vendas/ocupacao-mesas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numero").value(1))
                .andExpect(jsonPath("$[0].ocupada").value(true))
                .andExpect(jsonPath("$[1].numero").value(3))
                .andExpect(jsonPath("$[1].ocupada").value(false));
    }

    @Test
    @DisplayName("comanda cancelada não conta no total da venda")
    void totalIgnoraComandaCancelada() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        Long cancelada = criarComanda(mesa, 3);
        mudarStatus(cancelada, "CANCELADO").andExpect(status().isOk());

        get("/vendas/" + vendaId)
                .andExpect(jsonPath("$.valorTotal").value(25.9))
                .andExpect(jsonPath("$.comandas.length()").value(2));
    }

    // ------------------------------------------------------------------ mesa com venda aberta

    @Test
    @DisplayName("mesa com venda aberta não pode ser excluída")
    void mesaOcupadaNaoExclui() throws Exception {
        criarComanda(mesa, 1);

        assertRegraNegocio(delete("/mesa/" + mesa.getId()), "A mesa tem uma venda aberta e não pode ser excluída");
        assertThat(mesaRepository.findById(mesa.getId()).orElseThrow().getFlativo()).isEqualTo("S");
    }

    @Test
    @DisplayName("mesa sem venda aberta continua podendo ser excluída")
    void mesaLivreExclui() throws Exception {
        delete("/mesa/" + mesa.getId()).andExpect(status().isNoContent());

        assertThat(mesaRepository.findById(mesa.getId()).orElseThrow().getFlativo()).isEqualTo("N");
    }

    @Test
    @DisplayName("comanda não pode ser movida para outra mesa: 422")
    void comandaNaoMudaDeMesa() throws Exception {
        Mesa outra = criarMesa(2);
        Long comandaId = criarComanda(mesa, 1);

        assertRegraNegocio(put("/pedido/" + comandaId, corpoComanda(outra, 1)),
                "Não é possível mudar a comanda para outra mesa depois de lançada");
    }

    // ------------------------------------------------------------------ abertura explícita

    @Test
    @DisplayName("abrir venda para mesa que já tem venda aberta: 422")
    void abrirVendaDeMesaOcupada() throws Exception {
        criarComanda(mesa, 1);

        assertRegraNegocio(post("/vendas", "{\"mesaId\": %d}".formatted(mesa.getId())),
                "A mesa 1 já possui uma venda aberta");
        assertThat(vendaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("abrir venda para mesa livre: 201, ABERTA e mesa passa a ocupada")
    void abrirVendaDeMesaLivre() throws Exception {
        post("/vendas", "{\"mesaId\": %d, \"nomeCliente\": \"Maria\", \"observacao\": \"Aniversário\"}"
                .formatted(mesa.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.nomeCliente").value("Maria"))
                .andExpect(jsonPath("$.observacao").value("Aniversário"))
                .andExpect(jsonPath("$.valorTotal").value(0.0));

        assertThat(ocupacaoDe(mesa).get("ocupada")).isEqualTo(true);
    }

    @Test
    @DisplayName("venda sem mesa (delivery, retirada) é aceita")
    void vendaSemMesa() throws Exception {
        post("/vendas", "{\"nomeCliente\": \"Pedro (retirada)\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.mesaId").doesNotExist())
                .andExpect(jsonPath("$.numeroMesa").doesNotExist())
                .andExpect(jsonPath("$.nomeCliente").value("Pedro (retirada)"));
    }

    @Test
    @DisplayName("duas vendas sem mesa podem coexistir")
    void duasVendasSemMesa() throws Exception {
        post("/vendas", "{\"nomeCliente\": \"A\"}").andExpect(status().isCreated());
        post("/vendas", "{\"nomeCliente\": \"B\"}").andExpect(status().isCreated());

        assertThat(vendaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("abrir venda para mesa inexistente: 404")
    void abrirVendaMesaInexistente() throws Exception {
        assertNaoEncontrado(post("/vendas", "{\"mesaId\": %d}".formatted(ID_INEXISTENTE)),
                "Mesa não encontrado: " + ID_INEXISTENTE);
    }

    @Test
    @DisplayName("o banco impede duas vendas abertas na mesma mesa mesmo fora da regra do service")
    void bancoImpedeDuasVendasAbertas() throws Exception {
        criarComanda(mesa, 1);

        Venda duplicada = new Venda();
        duplicada.setTipo(TipoVendaEnum.MESA);
        duplicada.setMesa(mesa);
        duplicada.setMesaAbertaId(mesa.getId());
        duplicada.setStatus(StatusVendaEnum.ABERTA);
        duplicada.setUsuarioAbertura("outra.pessoa");

        assertThatThrownBy(() -> vendaRepository.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(vendaRepository.count()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ consultas e edição

    @Test
    @DisplayName("venda aberta da mesa: 204 quando não há e 200 com a venda quando há")
    void vendaAbertaDaMesa() throws Exception {
        get("/vendas/mesa/" + mesa.getId() + "/aberta").andExpect(status().isNoContent());

        Long vendaId = vendaDaNovaComanda(mesa, 1);

        get("/vendas/mesa/" + mesa.getId() + "/aberta")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendaId));
    }

    @Test
    @DisplayName("atualizar cliente e observação da venda aberta")
    void atualizarVenda() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);

        patch("/vendas/" + vendaId, "{\"nomeCliente\": \"Ana Souza\", \"observacao\": \"Sem cebola\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCliente").value("Ana Souza"))
                .andExpect(jsonPath("$.observacao").value("Sem cebola"));
    }

    @Test
    @DisplayName("venda inexistente: 404")
    void vendaInexistente() throws Exception {
        assertNaoEncontrado(get("/vendas/" + ID_INEXISTENTE), "Venda não encontrado: " + ID_INEXISTENTE);
    }

    @Test
    @DisplayName("sem token: venda barrada pela segurança")
    void semToken() throws Exception {
        assertBarradoPelaSeguranca(postSemToken("/vendas", "{}"));
    }
}
