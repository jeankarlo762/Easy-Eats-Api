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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vínculo produto × ingrediente (Produto natureza INSUMO) × quantidade
 * utilizada para produzir uma unidade do produto — a "ficha técnica"/receita.
 * Um ingrediente aparece no máximo uma vez por produto. Base para custo real
 * de produção, planejamento e, futuramente, baixa automática do Estoque do
 * ingrediente na venda (ainda não implementada).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBFICHATECNICAITEM", uniqueConstraints = @UniqueConstraint(columnNames = { "produto_id", "ingrediente_id" }))
public class FichaTecnicaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto ingrediente;

    @NotNull(message = "A quantidade utilizada é obrigatória")
    @Positive(message = "A quantidade utilizada deve ser maior que zero")
    private BigDecimal quantidadeUtilizada;
}
