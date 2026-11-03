package com.easy.eats.ingrediente.dto;

import java.math.BigDecimal;

import com.easy.eats.produto.enums.UnidadeMedida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record IngredientePayload(
        @NotBlank(message = "O nome do ingrediente é obrigatório") String nome,
        @NotNull(message = "A unidade de medida é obrigatória") UnidadeMedida unidadeMedida,
        @PositiveOrZero(message = "O custo não pode ser negativo") BigDecimal custo,
        Integer categoriaId,
        @NotNull(message = "A quantidade em estoque é obrigatória") @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa") Integer quantidadeEstoque,
        @NotNull(message = "O estoque mínimo é obrigatório") @PositiveOrZero(message = "O estoque mínimo não pode ser negativo") Integer estoqueMinimo) {
}
