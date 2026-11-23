package com.easyeats.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ingrediente e a quantidade usada em um produto")
public record IngredienteQuantidadeDto(

        @NotNull(message = "O ingrediente é obrigatório")
        @Schema(description = "Identificador do ingrediente", example = "1")
        Long ingredienteId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Schema(description = "Quantidade usada por unidade vendida, na unidade de medida do ingrediente (maior que zero)", example = "0.15")
        Double quantidade
) {
}
