package com.easy.eats.venda.quicksort;

import java.math.BigDecimal;
import java.util.List;

public class QuickSortProdutos {

    public void ordenar(List<ProdutoRanking> lista) {
        quickSort(lista, 0, lista.size() - 1);
    }

    private void quickSort(List<ProdutoRanking> lista, int inicio, int fim) {
        if (inicio < fim) {
            int indicePivo = particionar(lista, inicio, fim);
            quickSort(lista, inicio, indicePivo - 1);
            quickSort(lista, indicePivo + 1, fim);
        }
    }

    private int particionar(List<ProdutoRanking> lista, int inicio, int fim) {
        BigDecimal pivo = lista.get(fim).getFaturamentoTotal();
        int i = inicio - 1;

        for (int j = inicio; j < fim; j++) {
            if (lista.get(j).getFaturamentoTotal().compareTo(pivo) >= 0) {
                i++;
                ProdutoRanking temp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, temp);
            }
        }

        ProdutoRanking temp = lista.get(i + 1);
        lista.set(i + 1, lista.get(fim));
        lista.set(fim, temp);

        return i + 1;
    }

}
