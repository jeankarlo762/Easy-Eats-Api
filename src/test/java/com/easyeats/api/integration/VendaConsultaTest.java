package com.easyeats.api.integration;

import com.easyeats.api.entity.Mesa;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;
import com.easyeats.api.support.VendaTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Histórico de vendas (Administrador e Gerente): filtros, paginação e permissões. */
@DisplayName("Venda: histórico, filtros e permissões")
class VendaConsultaTest extends VendaTestBase {

    private static final LocalDateTime DIA_10 = LocalDateTime.of(2026, 3, 10, 12, 0);
    private static final LocalDateTime DIA_11 = LocalDateTime.of(2026, 3, 11, 12, 0);
    private static final LocalDateTime DIA_12 = LocalDateTime.of(2026, 3, 12, 12, 0);

    private Venda salvarVenda(Mesa daMesa, StatusVendaEnum status, String usuario, LocalDateTime abertura,
            String total) {
        Venda venda = new Venda();
        venda.setTipo(daMesa == null ? TipoVendaEnum.RETIRADA : TipoVendaEnum.MESA);
        venda.setMesa(daMesa);
        venda.setStatus(status);
        venda.setUsuarioAbertura(usuario);
        venda.setDataAbertura(abertura);
        venda.setNomeCliente("Cliente de " + usuario);
        if (status == StatusVendaEnum.ABERTA && daMesa != null) {
            venda.setMesaAbertaId(daMesa.getId());
        }
        if (status == StatusVendaEnum.FINALIZADA) {
            venda.setValorTotal(new BigDecimal(total));
            venda.setUsuarioFechamento(usuario);
            venda.setDataFechamento(abertura.plusHours(1));
        }
        return vendaRepository.save(venda);
    }

    private void massaDeVendas() {
        Mesa outra = criarMesa(2);
        salvarVenda(mesa, StatusVendaEnum.FINALIZADA, "ana", DIA_10, "50.00");
        salvarVenda(outra, StatusVendaEnum.FINALIZADA, "bia", DIA_11, "80.00");
        salvarVenda(mesa, StatusVendaEnum.CANCELADA, "ana", DIA_12, "0");
    }

    @Test
    @DisplayName("histórico: mais recentes primeiro, com paginação e total gravado das vendas finalizadas")
    void historicoPaginado() throws Exception {
        massaDeVendas();

        get("/vendas?pagina=0&tamanho=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].status").value("CANCELADA"))
                .andExpect(jsonPath("$.itens[1].status").value("FINALIZADA"))
                .andExpect(jsonPath("$.itens[1].valorTotal").value(80.0))
                .andExpect(jsonPath("$.itens[1].numeroMesa").value(2))
                .andExpect(jsonPath("$.itens[1].usuarioFechamento").value("bia"));
        get("/vendas?pagina=1&tamanho=2").andExpect(jsonPath("$.itens.length()").value(1));
    }

    @Test
    @DisplayName("histórico: filtros por status, mesa, usuário e período (pontas inclusivas)")
    void historicoComFiltros() throws Exception {
        massaDeVendas();

        get("/vendas?status=FINALIZADA").andExpect(jsonPath("$.total").value(2));
        get("/vendas?status=CANCELADA").andExpect(jsonPath("$.total").value(1));
        get("/vendas?mesaId=" + mesa.getId()).andExpect(jsonPath("$.total").value(2));
        get("/vendas?usuario=bia").andExpect(jsonPath("$.total").value(1));
        get("/vendas?inicio=2026-03-11&fim=2026-03-11").andExpect(jsonPath("$.total").value(1));
        get("/vendas?inicio=2026-03-11&fim=2026-03-12").andExpect(jsonPath("$.total").value(2));
        get("/vendas?status=FINALIZADA&usuario=ana&mesaId=" + mesa.getId()).andExpect(jsonPath("$.total").value(1));
        get("/vendas?inicio=2030-01-01").andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("histórico: venda aberta mostra o total calculado pelas comandas")
    void historicoComVendaAberta() throws Exception {
        criarComanda(mesa, 2);

        get("/vendas?status=ABERTA")
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].valorTotal").value(51.8));
    }

    @Test
    @DisplayName("histórico: tamanho de página acima do limite é reduzido para 100")
    void historicoLimitaTamanho() throws Exception {
        massaDeVendas();

        get("/vendas?tamanho=5000").andExpect(jsonPath("$.tamanho").value(100));
    }

    @Test
    @DisplayName("gerente acessa o histórico")
    void gerenteConsulta() throws Exception {
        getComo(tokenDe("gerente.teste", "GERENTE"), "/vendas").andExpect(status().isOk());
    }

    @Test
    @DisplayName("funcionário não acessa o histórico (403) mas consulta a venda aberta da mesa e a ocupação")
    void funcionarioConsultaApenasOperacao() throws Exception {
        Long vendaId = vendaDaNovaComanda(mesa, 1);
        String funcionario = tokenDe("func.teste", "FUNCIONARIO");

        getComo(funcionario, "/vendas")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida apenas para Administrador ou Gerente"));
        getComo(funcionario, "/vendas/ocupacao-mesas").andExpect(status().isOk());
        getComo(funcionario, "/vendas/mesa/" + mesa.getId() + "/aberta").andExpect(status().isOk());
        getComo(funcionario, "/vendas/" + vendaId).andExpect(status().isOk());
    }

    @Test
    @DisplayName("filtro de status inválido: 422 (handler de IllegalArgumentException)")
    void statusInvalido() throws Exception {
        get("/vendas?status=EXPLODIU").andExpect(status().isUnprocessableEntity());
    }
}
