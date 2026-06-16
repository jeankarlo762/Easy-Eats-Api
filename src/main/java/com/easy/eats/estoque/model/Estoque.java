package com.easy.eats.estoque.model;

import java.time.LocalDateTime;

import com.easy.eats.produto.model.Produto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBESTOQUE")
@Getter
@Setter
@Builder
public class Estoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Produto produto;

    private Integer quantidadeAtual;

    private Integer estoqueMinimo;

    private LocalDateTime dtUltimaMovimentacao;

}
