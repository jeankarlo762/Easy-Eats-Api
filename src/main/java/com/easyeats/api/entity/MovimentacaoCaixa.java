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

/**
 * Lançamento do caixa (fundo inicial, suprimento ou sangria). É imutável: não há alteração
 * nem exclusão, correções são feitas com um novo lançamento.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_movimentacao_caixa", indexes = {
        @Index(name = "idx_mov_caixa_caixa", columnList = "caixa_id"),
        @Index(name = "idx_mov_caixa_data", columnList = "data_movimentacao")
})
public class MovimentacaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoCaixaEnum tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    private String descricao;

    @Column(nullable = false)
    private String usuario;

    // Preenchido nos lançamentos do tipo VENDA, para rastrear a venda que originou a entrada
    @Column(name = "venda_id")
    private Long vendaId;

    @Column(name = "data_movimentacao", nullable = false, updatable = false)
    private LocalDateTime dataMovimentacao;

    @PrePersist
    public void antesDeCriar() {
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDateTime.now();
        }
    }
}
