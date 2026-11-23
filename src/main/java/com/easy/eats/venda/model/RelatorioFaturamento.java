package com.easy.eats.venda.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioFaturamento {

    private BigDecimal totalFaturado;
    private BigDecimal ticketMedio;
    private Integer quantidadeVendas;

}
