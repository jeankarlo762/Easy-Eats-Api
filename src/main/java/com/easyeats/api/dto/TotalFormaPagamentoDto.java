package com.easyeats.api.dto;

import com.easyeats.api.entity.FormaPagamentoEnum;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Total recebido por forma de pagamento")
public record TotalFormaPagamentoDto(
        @Schema(description = "Forma de pagamento", example = "PIX", accessMode = Schema.AccessMode.READ_ONLY)
        FormaPagamentoEnum forma,
        @Schema(description = "Total recebido nessa forma, em reais", example = "300.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal total
) {
}
