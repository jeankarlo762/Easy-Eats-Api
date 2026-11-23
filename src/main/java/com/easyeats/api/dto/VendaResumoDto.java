package com.easyeats.api.dto;

import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.OrigemVendaEnum;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.TipoVendaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de uma venda, usado nas listagens")
public record VendaResumoDto(
        @Schema(description = "Identificador único da venda", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Tipo da venda: MESA, RETIRADA ou DELIVERY", example = "MESA", accessMode = Schema.AccessMode.READ_ONLY)
        TipoVendaEnum tipo,
        @Schema(description = "Identificador da mesa. Vazio em RETIRADA", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Long mesaId,
        @Schema(description = "Número da mesa. Vazio em RETIRADA", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        Integer numeroMesa,
        @Schema(description = "Situação da venda: ABERTA, FINALIZADA ou CANCELADA", example = "FINALIZADA", accessMode = Schema.AccessMode.READ_ONLY)
        StatusVendaEnum status,
        @Schema(description = "Nome do cliente", example = "Ana", accessMode = Schema.AccessMode.READ_ONLY)
        String nomeCliente,
        @Schema(description = "Telefone do cliente (cardápio digital)", example = "11999998888", accessMode = Schema.AccessMode.READ_ONLY)
        String telefoneCliente,
        @Schema(description = "Endereço de entrega (delivery do cardápio digital)", accessMode = Schema.AccessMode.READ_ONLY)
        String enderecoEntrega,
        @Schema(description = "Forma de pagamento escolhida pelo cliente no cardápio digital", example = "PIX", accessMode = Schema.AccessMode.READ_ONLY)
        FormaPagamentoEnum formaPagamentoPrevista,
        @Schema(description = "Em dinheiro: valor que o cliente vai entregar, para o troco", example = "100.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal trocoPara,
        @Schema(description = "Origem da venda: SISTEMA ou CARDAPIO_DIGITAL", example = "CARDAPIO_DIGITAL", accessMode = Schema.AccessMode.READ_ONLY)
        OrigemVendaEnum origem,
        @Schema(description = "Usuário que abriu a venda", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioAbertura,
        @Schema(description = "Data e hora da abertura", example = "2026-03-10T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataAbertura,
        @Schema(description = "Usuário que finalizou ou cancelou a venda", example = "superadmin", accessMode = Schema.AccessMode.READ_ONLY)
        String usuarioFechamento,
        @Schema(description = "Data e hora da finalização ou do cancelamento", example = "2026-03-10T13:10:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataFechamento,
        @Schema(description = "Total da venda", example = "86.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorTotal
) {
}
