package com.easyeats.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Na entrada, apenas produtoId e quantidade são usados.
 * id, pedidoId e valorUnitario são preenchidos pela API (o valor vem do preço ativo do produto).
 */
@Schema(description = "Item de uma comanda. Na entrada, só produtoId e quantidade são usados")
public record ItemPedidoDto(
        @Schema(description = "Identificador único do item", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @Schema(description = "Identificador da comanda, preenchido pela API", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
        Long pedidoId,

        @NotNull(message = "O produto é obrigatório")
        @Schema(description = "Identificador do produto pedido", example = "1")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Schema(description = "Quantidade pedida, maior que zero", example = "2")
        Integer quantidade,

        @Schema(description = "Valor unitário, fixado pela API a partir do preço ativo do produto", example = "29.90", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorUnitario
) {
}
