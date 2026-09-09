package com.easy.eats.itemVenda.model;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import com.easy.eats.produto.model.Produto;
import com.easy.eats.venda.model.Venda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
@Table(name = "TBITEMVENDA")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Double quantidade;

    @NotNull(message = "O preço unitário é obrigatório")
    @Positive(message = "O preço unitário deve ser maior que zero")
    private BigDecimal preco_unitario;

    private BigDecimal custo_unitario;
    private BigDecimal valor_total;
    private BigDecimal desconto;
    private String dt_alteracao;

    /** Observação do item (ex.: "sem cebola", "bem passado"). */
    private String observacao;

    @NotNull(message = "A venda é obrigatória")
    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @JsonIgnoreProperties("itemVenda")
    @OneToMany(mappedBy = "itemVenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVendaComposicaoRemovida> composicaoRemovida = new ArrayList<>();

    @JsonIgnoreProperties("itemVenda")
    @OneToMany(mappedBy = "itemVenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVendaAdicional> adicionais = new ArrayList<>();
}
