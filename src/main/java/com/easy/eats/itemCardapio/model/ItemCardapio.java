package com.easy.eats.itemCardapio.model;

import java.math.BigDecimal;

import com.easy.eats.cardapio.model.Cardapio;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela de junção enriquecida entre Cardapio e Produto: além de listar o
 * produto no cardápio, guarda ordem de exibição e overrides específicos
 * daquele cardápio (preço/descrição/foto/disponibilidade), sem alterar o
 * Produto em si — o mesmo Produto pode aparecer em vários cardápios com
 * apresentações diferentes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBITEMCARDAPIO", uniqueConstraints = @UniqueConstraint(columnNames = { "cardapio_id", "produto_id" }))
public class ItemCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnoreProperties("itens")
    @ManyToOne
    @JoinColumn(name = "cardapio_id", nullable = false)
    private Cardapio cardapio;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    private Integer ordem;
    private Boolean disponivel;
    private BigDecimal precoOverride;
    private String descricaoOverride;
    private String fotoUrl;
}
