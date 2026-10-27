package com.easy.eats.fichatecnica.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FichaTecnicaItemPayload(
        @NotNull(message = "O ingrediente é obrigatório") Integer ingredienteId,
        @NotNull(message = "A quantidade utilizada é obrigatória") @Positive(message = "A quantidade utilizada deve ser maior que zero") BigDecimal quantidadeUtilizada) {
}
