package com.easy.eats.itemVenda.model;

import java.math.BigDecimal;

import com.easy.eats.adicional.model.Adicional;

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
 * Adicional escolhido para um item de venda. Nome e preço são copiados do
 * Adicional no momento da venda (snapshot), para não mudar retroativamente
 * se o cadastro do adicional for alterado depois — mesmo padrão já usado em
 * ProdutoRanking para preço/faturamento histórico.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBITEMVENDAADICIONAL")
public class ItemVendaAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "item_venda_id", nullable = false)
    private ItemVenda itemVenda;

    @NotNull(message = "O adicional é obrigatório")
    @ManyToOne
    @JoinColumn(name = "adicional_id", nullable = false)
    private Adicional adicional;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidade;

    private String nome;
    private BigDecimal preco;
}
