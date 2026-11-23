package com.easyeats.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastro e alteração de ingredientes")
public record IngredienteDto(
        @Schema(description = "Identificador único do ingrediente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Nome do ingrediente", example = "Queijo mussarela")
        @NotBlank(message = "O nome do ingrediente é obrigatório") String nome,
        @Schema(description = "Unidade de medida do ingrediente", example = "kg")
        @NotBlank(message = "A unidade de medida é obrigatória") String unidadeMedida,
        @NotNull(message = "O custo é obrigatório")
        @PositiveOrZero(message = "O custo não pode ser negativo")
        @Schema(description = "Custo por unidade de medida, em reais", example = "42.50")
        BigDecimal custo
) {
}
