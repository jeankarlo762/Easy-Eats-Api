package com.easyeats.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ficha técnica de um produto, com todos os ingredientes e quantidades")
public record ProdutoIngredienteLoteDto(

        @NotNull(message = "O produto é obrigatório")
        @Schema(description = "Identificador do produto", example = "1")
        Long produtoId,

        @NotEmpty(message = "Informe ao menos um ingrediente")
        @Valid
        @Schema(description = "Ingredientes usados pelo produto e a quantidade de cada um")
        List<IngredienteQuantidadeDto> ingredientes
) {
}
