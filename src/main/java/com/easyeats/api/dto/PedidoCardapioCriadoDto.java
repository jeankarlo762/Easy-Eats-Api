package com.easyeats.api.dto;

import java.math.BigDecimal;

import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.TipoVendaEnum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmação do pedido feito no cardápio digital")
public record PedidoCardapioCriadoDto(
        Long numeroPedido,
        TipoVendaEnum tipoEntrega,
        BigDecimal total,
        FormaPagamentoEnum formaPagamento
) {
}
