package com.easyeats.api.dto;

import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;

import java.math.BigDecimal;

public record TotalPorTipoDto(
        TipoMovimentacaoCaixaEnum tipo,
        BigDecimal total
) {
}
