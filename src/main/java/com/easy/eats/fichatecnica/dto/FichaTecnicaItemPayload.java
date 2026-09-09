package com.easy.eats.fichatecnica.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FichaTecnicaItemPayload(
        @NotNull(message = "O insumo é obrigatório") Integer insumoId,
        @NotNull(message = "A quantidade é obrigatória") @Positive(message = "A quantidade deve ser maior que zero") BigDecimal quantidade) {
}
