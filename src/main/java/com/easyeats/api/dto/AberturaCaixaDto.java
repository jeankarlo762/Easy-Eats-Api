package com.easyeats.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para abrir o caixa")
public record AberturaCaixaDto(
        @NotNull(message = "O fundo de caixa é obrigatório")
        @PositiveOrZero(message = "O fundo de caixa não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
        @Schema(description = "Dinheiro inicial no caixa, em reais, não negativo (até 2 casas decimais)", example = "100.00")
        BigDecimal fundoCaixa,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        @Schema(description = "Observação da abertura, com até 255 caracteres", example = "Turno da noite")
        String observacao
) {
}
