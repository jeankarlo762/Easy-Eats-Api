package com.easyeats.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quantidade de uma entrada ou saída de estoque")
public record MovimentacaoDto(
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "Use no máximo 3 casas decimais")
        @Schema(description = "Quantidade movimentada, maior que zero (até 3 casas decimais)", example = "1.500")
        BigDecimal quantidade
) {
}
