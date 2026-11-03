package com.easy.eats.ingrediente.dto;

import java.math.BigDecimal;

import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.produto.enums.UnidadeMedida;
import com.easy.eats.produto.model.Produto;

/** Ingrediente = Produto de natureza INSUMO + seu registro de Estoque (quando houver). */
public record IngredienteResponse(
        Integer id,
        String nome,
        UnidadeMedida unidadeMedida,
        BigDecimal custo,
        Integer categoriaId,
        String categoriaNome,
        Integer quantidadeEstoque,
        Integer estoqueMinimo) {

    public static IngredienteResponse de(Produto produto, Estoque estoque) {
        return new IngredienteResponse(
                produto.getId(),
                produto.getNome(),
                produto.getUnidadeMedida(),
                produto.getCusto(),
                produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                produto.getCategoria() != null ? produto.getCategoria().getNome() : null,
                estoque != null ? estoque.getQuantidadeAtual() : null,
                estoque != null ? estoque.getEstoqueMinimo() : null);
    }
}
