package com.easy.eats.cardapiopublico.dto;

import java.util.List;

/**
 * Pedido feito pelo cliente no link público. Só ids e quantidades: o preço é
 * sempre recalculado no servidor a partir do Produto, nunca aceito do corpo.
 */
public record PedidoPublicoRequest(String nomeCliente, Integer mesaId, List<Item> itens) {

    public record Item(Integer produtoId, Integer quantidade, String observacao) {
    }
}
