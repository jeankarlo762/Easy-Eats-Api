package com.easyeats.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para fechar o caixa")
public record FechamentoCaixaDto(
        @NotNull(message = "O valor contado é obrigatório")
        @PositiveOrZero(message = "O valor contado não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
        @Schema(description = "Dinheiro contado no caixa, em reais, não negativo (até 2 casas decimais)", example = "348.00")
        BigDecimal valorContado,

        // Obrigatória quando o valor contado difere do esperado pelo sistema
        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        @Schema(description = "Justificativa da diferença. Obrigatória quando o valor contado difere do esperado", example = "Troco devolvido a mais")
        String observacao
) {
}
