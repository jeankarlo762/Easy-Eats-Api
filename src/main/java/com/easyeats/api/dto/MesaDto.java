package com.easyeats.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastro e alteração de mesas")
public record MesaDto(
        @Schema(description = "Identificador único da mesa", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "O número da mesa é obrigatório")
        @Positive(message = "O número da mesa deve ser maior que zero")
        @Schema(description = "Número da mesa, maior que zero e único", example = "5")
        Integer numero,

        @Schema(description = "Indica se a mesa está ativa (S ou N)", example = "S")
        String flativo
) {
}
