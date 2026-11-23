package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Faturamento de um dia")
public record TotalDiaDto(
        @Schema(description = "Dia do faturamento", example = "2026-03-10", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDate dia,
        @Schema(description = "Quantidade de vendas finalizadas no dia", example = "8", accessMode = Schema.AccessMode.READ_ONLY)
        Long quantidade,
        @Schema(description = "Total faturado no dia, em reais", example = "400.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal total
) {
}
