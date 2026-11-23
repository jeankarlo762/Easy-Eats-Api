package com.easyeats.api.dto;

import java.math.BigDecimal;

public record TotalPeriodoDto(
        Long quantidade,
        BigDecimal total
) {
}