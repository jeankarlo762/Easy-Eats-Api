package com.easyeats.api.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/** Situação de uma mesa: ocupada enquanto existir uma venda aberta nela. */
@Schema(description = "Situação de uma mesa: ocupada enquanto houver venda aberta nela")
public record OcupacaoMesaDto(
        @Schema(description = "Identificador da mesa", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Long mesaId,
        @Schema(description = "Número da mesa", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Integer numero,
        @Schema(description = "Indica se a mesa tem venda aberta", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
        boolean ocupada,
        @Schema(description = "Venda aberta da mesa. Vazio quando livre", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
        Long vendaId,
        @Schema(description = "Nome do cliente da venda aberta", example = "Ana", accessMode = Schema.AccessMode.READ_ONLY)
        String nomeCliente,
        @Schema(description = "Quantidade de comandas não canceladas da venda aberta", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
        Long quantidadeComandas,
        @Schema(description = "Total parcial da venda aberta", example = "86.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorParcial
) {
}
