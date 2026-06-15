package com.easy.eats.estoque.model;

import java.time.LocalDateTime;

import com.easy.eats.produto.model.Produto;

public class Estoque {

    private Integer id;

    private Produto produto;

    private Integer quantidadeAtual;

    private Integer estoqueMinimo;

    private LocalDateTime dtUltimaMovimentacao;

}
