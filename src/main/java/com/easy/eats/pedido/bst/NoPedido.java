package com.easy.eats.pedido.bst;

import com.easy.eats.pedido.model.Pedido;

public class NoPedido {

    Pedido pedido;
    NoPedido esquerda;
    NoPedido direita;
    int altura;

    public NoPedido(Pedido pedido) {
        this.pedido = pedido;
        this.altura = 1;
    }
}