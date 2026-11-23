package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Relatório de um caixa fechado")
public record RelatorioFechamentoDto(
        @Schema(description = "Dados do caixa", accessMode = Schema.AccessMode.READ_ONLY)
        CaixaDto caixa,
        @Schema(description = "Soma dos suprimentos", example = "50.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalSuprimentos,
        @Schema(description = "Soma das sangrias", example = "20.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalSangrias,
        @Schema(description = "Soma das vendas em dinheiro", example = "220.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal totalVendas,
        @Schema(description = "Lançamentos do caixa", accessMode = Schema.AccessMode.READ_ONLY)
        List<MovimentacaoCaixaDto> movimentacoes
) {
}
