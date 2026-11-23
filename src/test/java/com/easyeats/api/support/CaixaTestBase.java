package com.easyeats.api.support;

import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.MovimentacaoCaixa;
import com.easyeats.api.entity.StatusCaixaEnum;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Helpers compartilhados pelos testes de Caixa. */
public abstract class CaixaTestBase extends IntegrationTestBase {

    /** Usuário do token padrão dos testes (perfil ADMIN). */
    protected static final String USUARIO_PADRAO = "usuario.teste";

    protected static String jsonAbertura(String fundo) {
        return "{\"fundoCaixa\": %s}".formatted(fundo);
    }

    protected static String jsonMovimentacao(String tipo, String valor, String descricao) {
        return "{\"tipo\": \"%s\", \"valor\": %s, \"descricao\": \"%s\"}".formatted(tipo, valor, descricao);
    }

    protected static String jsonFechamento(String valorContado, String observacao) {
        return "{\"valorContado\": %s, \"observacao\": \"%s\"}".formatted(valorContado, observacao);
    }

    /** Abre um caixa pela API (usuário padrão) e devolve o id. */
    protected Long abrirCaixa(String fundo) throws Exception {
        return idDaResposta(post("/caixa/abrir", jsonAbertura(fundo)).andExpect(status().isCreated()));
    }

    /** Registra um lançamento pela API (usuário padrão). */
    protected void lancar(Long caixaId, String tipo, String valor, String descricao) throws Exception {
        post("/caixa/%d/movimentacoes".formatted(caixaId), jsonMovimentacao(tipo, valor, descricao))
                .andExpect(status().isCreated());
    }

    /** Grava um caixa direto no banco, com data de abertura controlada pelo teste. */
    protected Caixa salvarCaixa(StatusCaixaEnum status, String usuarioAbertura, LocalDateTime abertura) {
        Caixa caixa = new Caixa();
        caixa.setStatus(status);
        caixa.setFundoCaixa(new BigDecimal("100.00"));
        caixa.setUsuarioAbertura(usuarioAbertura);
        caixa.setDataAbertura(abertura);
        if (status == StatusCaixaEnum.ABERTO) {
            caixa.setAbertoUnico(true);
        } else {
            caixa.setUsuarioFechamento(usuarioAbertura);
            caixa.setDataFechamento(abertura.plusHours(8));
            caixa.setValorEsperado(new BigDecimal("100.00"));
            caixa.setValorContado(new BigDecimal("100.00"));
            caixa.setDiferenca(BigDecimal.ZERO.setScale(2));
        }
        return caixaRepository.save(caixa);
    }

    /** Grava um lançamento direto no banco, com data controlada pelo teste. */
    protected MovimentacaoCaixa salvarMovimentacao(Caixa caixa, TipoMovimentacaoCaixaEnum tipo, String valor,
            String usuario, LocalDateTime data) {
        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
        movimentacao.setCaixa(caixa);
        movimentacao.setTipo(tipo);
        movimentacao.setValor(new BigDecimal(valor));
        movimentacao.setDescricao("Lançamento de teste");
        movimentacao.setUsuario(usuario);
        movimentacao.setDataMovimentacao(data);
        return movimentacaoCaixaRepository.save(movimentacao);
    }
}
