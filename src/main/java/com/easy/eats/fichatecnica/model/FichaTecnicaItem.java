package com.easy.eats.fichatecnica.model;

import java.math.BigDecimal;

import com.easy.eats.produto.model.Produto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Um insumo (Produto natureza INSUMO) e a quantidade dele consumida para
 * produzir uma unidade de um produto PREPARADO — a "ficha técnica"/receita.
 * Base para custo real de produção; ainda não debita o Estoque do insumo na
 * venda (isso ficaria pra quando o módulo de Estoque cobrir baixa automática).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBFICHATECNICAITEM")
public class FichaTecnicaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto insumo;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private BigDecimal quantidade;
}
