package com.easy.eats.venda.quicksort;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRanking {

    private String nomeProduto;
    private Double quantidadeVendida;
    private BigDecimal faturamentoTotal;

}
