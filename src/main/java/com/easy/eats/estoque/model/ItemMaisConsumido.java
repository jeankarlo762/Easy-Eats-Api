package com.easy.eats.estoque.model;

/** Projeção usada no relatório de estoque: produtos com mais saídas registradas. */
public record ItemMaisConsumido(String nomeProduto, Long quantidadeConsumida) {
}
