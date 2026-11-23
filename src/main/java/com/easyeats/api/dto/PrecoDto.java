package com.easyeats.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastro e alteração de preços de venda")
public record PrecoDto(

        @Schema(description = "Identificador único do preço", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Schema(description = "Valor de venda, em reais (maior que zero)", example = "29.90")
        BigDecimal valor,

        @Schema(description = "Identificador do produto que recebe o preço", example = "1")
        Long produtoId

) {
}
