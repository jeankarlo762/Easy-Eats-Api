package com.easy.eats.produto.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBPRODUTO")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @PositiveOrZero(message = "O preço não pode ser negativo")
    private BigDecimal preco;

    @PositiveOrZero(message = "O custo não pode ser negativo")
    private BigDecimal custo;

    private Boolean flAtivo;
    private LocalDateTime dtAlteracao;

    /**
     * Distingue como o produto se comporta no pedido: só produtos PREPARADO
     * exibem composição editável e adicionais no carrinho (ver alerta de
     * modelagem do módulo — INSUMO e REVENDA não têm essas seções).
     */
    @Enumerated(EnumType.STRING)
    private NaturezaProduto natureza;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties("produtos")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

}