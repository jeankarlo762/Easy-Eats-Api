package com.easyeats.api.dto;

import com.easyeats.api.entity.StatusCaixaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Caixa, aberto ou fechado")
public record CaixaDto(
        @Schema(description = "Identificador único do caixa", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Situação do caixa: ABERTO ou FECHADO", example = "ABERTO", accessMode = Schema.AccessMode.READ_ONLY)
        StatusCaixaEnum status,
        @Schema(description = "Fundo inicial informado na abertura", example = "100.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal fundoCaixa,
        @Schema(description = "Usuário que abriu o caixa", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioAbertura,
        @Schema(description = "Data e hora da abertura", example = "2026-03-10T08:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataAbertura,
        @Schema(description = "Observação informada na abertura", example = "Turno da noite", accessMode = Schema.AccessMode.READ_ONLY)
        String observacaoAbertura,
        @Schema(description = "Usuário que fechou o caixa", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioFechamento,
        @Schema(description = "Data e hora do fechamento", example = "2026-03-10T22:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataFechamento,
        @Schema(description = "Valor que o sistema esperava ter no caixa ao fechar", example = "350.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorEsperado,
        @Schema(description = "Valor contado pelo operador no fechamento", example = "348.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorContado,
        @Schema(description = "Diferença entre o valor contado e o esperado", example = "-2.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal diferenca,
        @Schema(description = "Justificativa da diferença, informada no fechamento", example = "Troco devolvido a mais", accessMode = Schema.AccessMode.READ_ONLY)
        String observacaoFechamento,
        @Schema(description = "Saldo atual, calculado pela API: fundo + suprimentos - sangrias + vendas em dinheiro", example = "250.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal saldoAtual       // calculado pela API: fundo + suprimentos - sangrias
) {
}
