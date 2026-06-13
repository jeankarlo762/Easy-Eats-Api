package com.easy.eats.itemVenda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBITEMVENDA")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Double quantidade;
    private Double preco_unitario;
    private Double custo_unitario;
    private Double valor_total;
    private Double desconto;
    private String dt_alteracao;

    public ItemVenda(Integer id, Double quantidade, Double preco_unitario, Double custo_unitario, Double valor_total,
            Double desconto, String dt_alteracao) {
        this.id = id;
        this.quantidade = quantidade;
        this.preco_unitario = preco_unitario;
        this.custo_unitario = custo_unitario;
        this.valor_total = valor_total;
        this.desconto = desconto;
        this.dt_alteracao = dt_alteracao;
    }

    

}
