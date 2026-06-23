package com.easy.eats.pedido.bst;

import com.easy.eats.pedido.model.Pedido;

public class ArvorePedido {

    private NoPedido raiz;

    public void inserir(Pedido pedido) {
        raiz = inserirRecursivo(raiz, pedido);
    }

    private NoPedido inserirRecursivo(NoPedido atual, Pedido pedido) {
        if (atual == null) {
            return new NoPedido(pedido);
        }

        if (pedido.getId() < atual.pedido.getId()) {
            atual.esquerda = inserirRecursivo(atual.esquerda, pedido);
        } else if (pedido.getId() > atual.pedido.getId()) {
            atual.direita = inserirRecursivo(atual.direita, pedido);
        } else {
            return atual;
        }

        atual.altura = 1 + Math.max(alturaAVL(atual.esquerda), alturaAVL(atual.direita));
        int balanceamento = getFatorBalanceamento(atual);

        if (balanceamento > 1 && pedido.getId() < atual.esquerda.pedido.getId())
            return rotacaoDireita(atual);

        if (balanceamento < -1 && pedido.getId() > atual.direita.pedido.getId())
            return rotacaoEsquerda(atual);

        if (balanceamento > 1 && pedido.getId() > atual.esquerda.pedido.getId()) {
            atual.esquerda = rotacaoEsquerda(atual.esquerda);
            return rotacaoDireita(atual);
        }

        if (balanceamento < -1 && pedido.getId() < atual.direita.pedido.getId()) {
            atual.direita = rotacaoDireita(atual.direita);
            return rotacaoEsquerda(atual);
        }

        return atual;
    }

    public Pedido buscar(Integer id) {
        return buscarRecursivo(raiz, id);
    }

    private Pedido buscarRecursivo(NoPedido atual, Integer id) {
        if (atual == null) {
            return null;
        }

        if (id.equals(atual.pedido.getId())) {
            return atual.pedido;
        }

        if (id < atual.pedido.getId()) {
            return buscarRecursivo(atual.esquerda, id);
        }

        return buscarRecursivo(atual.direita, id);
    }

    private int alturaAVL(NoPedido N) {
        if (N == null)
            return 0;
        return N.altura;
    }

    private int getFatorBalanceamento(NoPedido N) {
        if (N == null)
            return 0;
        return alturaAVL(N.esquerda) - alturaAVL(N.direita);
    }

    private NoPedido rotacaoDireita(NoPedido y) {
        NoPedido x = y.esquerda;
        NoPedido T2 = x.direita;

        x.direita = y;
        y.esquerda = T2;

        y.altura = Math.max(alturaAVL(y.esquerda), alturaAVL(y.direita)) + 1;
        x.altura = Math.max(alturaAVL(x.esquerda), alturaAVL(x.direita)) + 1;

        return x;
    }

    private NoPedido rotacaoEsquerda(NoPedido x) {
        NoPedido y = x.direita;
        NoPedido T2 = y.esquerda;

        y.esquerda = x;
        x.direita = T2;

        x.altura = Math.max(alturaAVL(x.esquerda), alturaAVL(x.direita)) + 1;
        y.altura = Math.max(alturaAVL(y.esquerda), alturaAVL(y.direita)) + 1;

        return y;
    }
}