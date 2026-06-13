package com.easy.eats.movimentacaoFinanceira.model;

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
@Table(name = "TBMOVIMENTACAO_FINANCEIRA")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String tipo;
    private String categoria;
    private Integer valor;
    private String descricao;
    private String dt_alteracao;

    public MovimentacaoFinanceira(Integer id, String tipo, String categoria, Integer valor, String descricao,
            String dt_alteracao) {
        this.id = id;
        this.tipo = tipo;
        this.categoria = categoria;
        this.valor = valor;
        this.descricao = descricao;
        this.dt_alteracao = dt_alteracao;
    }
    


}
