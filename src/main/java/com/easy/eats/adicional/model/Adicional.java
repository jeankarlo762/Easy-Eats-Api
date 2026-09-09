package com.easy.eats.adicional.model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Adicional disponível para um produto (ex.: "Bacon" no X-Burger), cadastrado
 * junto ao produto e oferecido como opção no carrinho.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBADICIONAL")
public class Adicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do adicional é obrigatório")
    private String nome;

    @NotNull(message = "O preço é obrigatório")
    @PositiveOrZero(message = "O preço não pode ser negativo")
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto produto;
}
