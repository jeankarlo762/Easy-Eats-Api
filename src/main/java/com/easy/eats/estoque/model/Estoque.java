package com.easy.eats.estoque.model;

import java.time.LocalDateTime;

import com.easy.eats.produto.model.Produto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nível de estoque de um Produto (tipicamente de natureza INSUMO, mas não
 * restrito a isso). Um registro por produto — um segundo POST para o mesmo
 * produto deve atualizar a quantidade existente, não criar um duplicado.
 */
@Entity
@Table(name = "TBESTOQUE", uniqueConstraints = @UniqueConstraint(columnNames = "produto_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull(message = "A quantidade atual é obrigatória")
    @PositiveOrZero(message = "A quantidade atual não pode ser negativa")
    private Integer quantidadeAtual;

    @NotNull(message = "O estoque mínimo é obrigatório")
    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo;

    private LocalDateTime dtUltimaMovimentacao;

}
