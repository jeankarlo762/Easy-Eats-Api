package com.easyeats.api.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Totais dos lançamentos filtrados")
public record ResumoLancamentosDto(
        @Schema(description = "Soma das entradas", example = "420.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalEntradas,
        @Schema(description = "Soma das saídas", example = "20.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalSaidas,
        @Schema(description = "Entradas menos saídas", example = "400.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal saldo
) {
}
