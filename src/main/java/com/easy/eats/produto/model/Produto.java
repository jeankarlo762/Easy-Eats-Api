package com.easy.eats.produto.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBPRODUTO")
public class Produto {

    private Integer id;
    private String nome;
    private String descricao;
    private Double preco;
    private Double custo;
    private Boolean flAtivo;
    private LocalDateTime dtAlteracao;

}