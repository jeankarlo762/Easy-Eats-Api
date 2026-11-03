package com.easy.eats.cardapiopublico.dto;

import java.math.BigDecimal;

public record PedidoPublicoResponse(Integer numeroPedido, String status, BigDecimal total) {
}
