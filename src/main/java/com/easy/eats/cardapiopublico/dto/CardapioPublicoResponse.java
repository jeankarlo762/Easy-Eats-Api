package com.easy.eats.cardapiopublico.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta do cardápio aberto pelo QR Code. DTO próprio (e não a entidade
 * Produto) porque a rota é pública: a entidade carrega custo e a Empresa
 * inteira (CNPJ, e-mail), que não podem vazar para quem não está logado.
 */
public record CardapioPublicoResponse(EmpresaPublica empresa, MesaPublica mesa, List<ProdutoPublico> produtos) {

    public record EmpresaPublica(String nome, String slug, String horarioFuncionamento) {
    }

    public record MesaPublica(Integer id, Integer numero) {
    }

    public record ProdutoPublico(Integer id, String nome, String descricao, BigDecimal preco, String categoria) {
    }
}
