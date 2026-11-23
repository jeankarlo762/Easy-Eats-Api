package com.easyeats.api.dto;

import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lançamento do caixa (suprimento, sangria, fundo inicial ou venda)")
public record MovimentacaoCaixaDto(
        @Schema(description = "Identificador único do lançamento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Caixa do lançamento, definido pela API", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long caixaId,               // definido pela API, o que vier do cliente é ignorado
        @Schema(description = "Venda de origem, definida pela API nos lançamentos do tipo VENDA", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
        Long vendaId,               // definido pela API nos lançamentos do tipo VENDA

        @NotNull(message = "O tipo é obrigatório")
        @Schema(description = "Tipo do lançamento. Manualmente só são aceitos SUPRIMENTO e SANGRIA", example = "SUPRIMENTO")
        TipoMovimentacaoCaixaEnum tipo,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
        @Schema(description = "Valor em reais, maior que zero (até 2 casas decimais)", example = "50.00")
        BigDecimal valor,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        @Schema(description = "Descrição ou motivo, com até 255 caracteres. Obrigatória na sangria", example = "Troco para o caixa")
        String descricao,

        @Schema(description = "Usuário autenticado que fez o lançamento, definido pela API", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuario,             // definido pela API (usuário autenticado)
        @Schema(description = "Data e hora do lançamento", example = "2026-03-10T12:30:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataMovimentacao
) {
}
