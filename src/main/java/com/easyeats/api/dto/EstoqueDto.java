package com.easyeats.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estoque de um ingrediente")
public record EstoqueDto(

        @Schema(description = "Identificador único do estoque", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "A quantidade atual é obrigatória")
        @PositiveOrZero(message = "A quantidade atual não pode ser negativa")
        @Digits(integer = 9, fraction = 3, message = "Use no máximo 3 casas decimais")
        @Schema(description = "Quantidade atual em estoque, na unidade de medida do ingrediente (até 3 casas decimais)", example = "12.500")
        BigDecimal qtdAtual,

        @NotNull(message = "A quantidade mínima é obrigatória")
        @PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
        @Digits(integer = 9, fraction = 3, message = "Use no máximo 3 casas decimais")
        @Schema(description = "Quantidade mínima desejada. Abaixo dela o estoque entra em alerta", example = "2.000")
        BigDecimal qtdMinima,

        @Schema(description = "Data e hora da última atualização do estoque", example = "2026-03-10T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dtAtualizacao,

        @NotNull(message = "O ingrediente é obrigatório")
        @Schema(description = "Identificador do ingrediente deste estoque", example = "1")
        Long idIngrediente,

        @Schema(description = "Nome do ingrediente, preenchido pela API", example = "Queijo mussarela", accessMode = Schema.AccessMode.READ_ONLY)
        String nomeIngrediente,

        @Schema(description = "Indica se a quantidade atual está igual ou abaixo da mínima. Calculado pela API", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
        Boolean abaixoDoMinimo      // calculado pela API, o que vier do cliente é ignorado
) {
}
