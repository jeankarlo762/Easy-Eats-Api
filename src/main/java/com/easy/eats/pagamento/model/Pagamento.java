package com.easy.eats.pagamento.model;

import java.math.BigDecimal;

import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.venda.model.Venda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBPAGAMENTO")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O método de pagamento é obrigatório")
    private String metodo;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotBlank(message = "O status é obrigatório")
    private String status;

    private String dt_pagamento;
    private String dt_alteracao;

    // Exatamente um entre venda/comanda deve estar preenchido — validado no
    // service, não aqui, porque a regra depende de qual dos dois veio no
    // payload (pagamento avulso de balcão vs. fechamento de comanda).
    @JsonIgnoreProperties("pagamentos")
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @JsonIgnoreProperties("vendas")
    @ManyToOne
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    // Sessão de caixa aberta no momento do pagamento — setada automaticamente
    // pelo PagamentoService, não vem no payload do cliente. Fica nula para
    // pagamentos do link público (sem caixa físico).
    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;
}
