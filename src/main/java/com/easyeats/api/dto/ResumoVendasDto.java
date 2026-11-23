package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/** Faturamento de um período: vendas finalizadas (por data de fechamento) e canceladas. */
@Schema(description = "Faturamento de um período")
public record ResumoVendasDto(
        @Schema(description = "Soma das vendas finalizadas no período", example = "1250.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalFaturado,
        @Schema(description = "Quantidade de vendas finalizadas", example = "25", accessMode = Schema.AccessMode.READ_ONLY)
        long quantidadeVendas,
        @Schema(description = "Valor médio por venda finalizada", example = "50.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal ticketMedio,
        @Schema(description = "Quantidade de vendas canceladas no período", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
        long quantidadeCanceladas,
        @Schema(description = "Total recebido por forma de pagamento", accessMode = Schema.AccessMode.READ_ONLY)
        List<TotalFormaPagamentoDto> porFormaPagamento,
        @Schema(description = "Total faturado por dia", accessMode = Schema.AccessMode.READ_ONLY)
        List<TotalDiaDto> porDia
) {
}
