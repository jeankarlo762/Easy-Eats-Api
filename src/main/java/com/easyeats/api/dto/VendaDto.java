package com.easyeats.api.dto;

import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.TipoVendaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Venda completa, com as comandas e os pagamentos")
public record VendaDto(
        @Schema(description = "Identificador único da venda", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Tipo da venda: MESA, RETIRADA ou DELIVERY", example = "MESA", accessMode = Schema.AccessMode.READ_ONLY)
        TipoVendaEnum tipo,
        @Schema(description = "Identificador da mesa. Vazio em RETIRADA", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Long mesaId,
        @Schema(description = "Número da mesa. Vazio em RETIRADA", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Integer numeroMesa,
        @Schema(description = "Situação da venda: ABERTA, FINALIZADA ou CANCELADA", example = "ABERTA", accessMode = Schema.AccessMode.READ_ONLY)
        StatusVendaEnum status,
        @Schema(description = "Nome do cliente", example = "Ana", accessMode = Schema.AccessMode.READ_ONLY)
        String nomeCliente,
        @Schema(description = "Observação da venda", example = "Cliente alérgico a amendoim", accessMode = Schema.AccessMode.READ_ONLY)
        String observacao,
        @Schema(description = "Usuário que abriu a venda", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioAbertura,
        @Schema(description = "Data e hora da abertura", example = "2026-03-10T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataAbertura,
        @Schema(description = "Usuário que finalizou ou cancelou a venda", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioFechamento,
        @Schema(description = "Data e hora da finalização ou do cancelamento", example = "2026-03-10T13:10:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataFechamento,
        @Schema(description = "Total da venda. Aberta: soma das comandas não canceladas. Finalizada: valor gravado", example = "86.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorTotal,      // aberta: soma das comandas não canceladas; finalizada: valor gravado
        @Schema(description = "Soma dos pagamentos registrados", example = "36.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorPago,
        @Schema(description = "Quanto ainda falta pagar", example = "50.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorRestante,
        @Schema(description = "Caixa em que a venda foi finalizada", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long caixaId,
        @Schema(description = "Motivo informado no cancelamento", example = "Cliente desistiu do pedido", accessMode = Schema.AccessMode.READ_ONLY)
        String motivoCancelamento,
        @Schema(description = "Comandas lançadas na venda", accessMode = Schema.AccessMode.READ_ONLY)
        List<PedidoDto> comandas,
        @Schema(description = "Pagamentos registrados na venda", accessMode = Schema.AccessMode.READ_ONLY)
        List<PagamentoVendaDto> pagamentos
) {
}
