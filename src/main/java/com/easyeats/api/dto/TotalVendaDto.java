package com.easyeats.api.dto;

import java.math.BigDecimal;

public record TotalVendaDto(
        Long vendaId,
        Long quantidadeComandas,
        BigDecimal total
) {
}