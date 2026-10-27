package com.easy.eats.estoque.dto;

import java.math.BigDecimal;

import com.easy.eats.produto.enums.UnidadeMedida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cadastro/edição de ingrediente: cria/atualiza o Produto INSUMO e o seu
 * registro de estoque juntos. quantidadeAtual só é usada na criação — depois
 * o saldo muda apenas por movimentação, para bater com o histórico.
 */
public record IngredientePayload(
        @NotBlank(message = "O nome do ingrediente é obrigatório") String nome,
        @NotNull(message = "A unidade de medida é obrigatória") UnidadeMedida unidadeMedida,
        @PositiveOrZero(message = "O custo não pode ser negativo") BigDecimal custo,
        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa") Integer quantidadeAtual,
        @NotNull(message = "O estoque mínimo é obrigatório") @PositiveOrZero(message = "O estoque mínimo não pode ser negativo") Integer estoqueMinimo) {
}
