package com.easyeats.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pagamento de uma venda. Uma venda pode ter vários (divisão da conta por forma de pagamento). */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_pagamento_venda", indexes = {
        @Index(name = "idx_pagamento_venda_venda", columnList = "venda_id")
})
public class PagamentoVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormaPagamentoEnum forma;

    // Parte da conta quitada por este pagamento
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    // Só em dinheiro: o que o cliente entregou. Troco = valorRecebido - valor
    @Column(name = "valor_recebido", precision = 12, scale = 2)
    private BigDecimal valorRecebido;

    @Column(nullable = false)
    private String usuario;

    @Column(name = "data_pagamento", nullable = false, updatable = false)
    private LocalDateTime dataPagamento;

    @PrePersist
    public void antesDeCriar() {
        if (dataPagamento == null) {
            dataPagamento = LocalDateTime.now();
        }
    }
}
