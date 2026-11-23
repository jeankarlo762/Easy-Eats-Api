package com.easyeats.api.dto;

import com.easyeats.api.entity.FormaPagamentoEnum;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagamento de uma venda")
public record PagamentoVendaDto(
        @Schema(description = "Identificador único do pagamento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "A forma de pagamento é obrigatória")
        @Schema(description = "Forma de pagamento: DINHEIRO, PIX, CREDITO, DEBITO ou OUTROS", example = "DINHEIRO")
        FormaPagamentoEnum forma,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
        @Schema(description = "Valor pago, em reais, maior que zero (até 2 casas decimais). Não pode passar do valor restante da venda", example = "50.00")
        BigDecimal valor,

        // Só para dinheiro: quanto o cliente entregou. Se omitido, assume o próprio valor.
        @Positive(message = "O valor recebido deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
        @Schema(description = "Só para dinheiro: quanto o cliente entregou. Se omitido, vale o próprio valor. Não pode ser menor que o valor", example = "100.00")
        BigDecimal valorRecebido,

        @Schema(description = "Troco, calculado pela API (valor recebido menos valor)", example = "50.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal troco,           // calculado pela API
        @Schema(description = "Usuário autenticado que registrou o pagamento, definido pela API", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuario,             // definido pela API (usuário autenticado)
        @Schema(description = "Data e hora do registro do pagamento", example = "2026-03-10T12:40:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataPagamento
) {
}
