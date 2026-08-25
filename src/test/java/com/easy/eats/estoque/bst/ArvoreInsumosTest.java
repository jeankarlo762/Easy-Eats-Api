package com.easy.eats.estoque.bst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.easy.eats.estoque.model.Insumo;

/**
 * O arquivo existia vazio (0 bytes) no repositório, então o módulo de estoque
 * não tinha nenhuma cobertura. Os casos abaixo exercitam a remoção de nó com
 * dois filhos — a parte da árvore com lógica real (substituição pelo sucessor
 * em-ordem) — além da busca case-insensitive que o serviço depende.
 */
class ArvoreInsumosTest {

    private ArvoreInsumos arvore;

    @BeforeEach
    void montarArvore() {
        arvore = new ArvoreInsumos();
        arvore.inserir(new Insumo("Queijo", 10, "kg"));
        arvore.inserir(new Insumo("Bacon", 5, "kg"));
        arvore.inserir(new Insumo("Tomate", 8, "kg"));
        arvore.inserir(new Insumo("Alface", 3, "un"));
        arvore.inserir(new Insumo("Cebola", 7, "kg"));
    }

    @Test
    @DisplayName("encontra um insumo inserido")
    void buscaInsumoExistente() {
        Insumo encontrado = arvore.buscar("Bacon");

        assertNotNull(encontrado);
        assertEquals("Bacon", encontrado.getNome());
        assertEquals(5, encontrado.getQuantidade());
    }

    @Test
    @DisplayName("a busca ignora maiúsculas e minúsculas")
    void buscaIgnoraCaixa() {
        assertNotNull(arvore.buscar("bacon"));
        assertNotNull(arvore.buscar("QUEIJO"));
    }

    @Test
    @DisplayName("devolve null para insumo inexistente")
    void buscaInsumoInexistente() {
        assertNull(arvore.buscar("Picanha"));
    }

    @Test
    @DisplayName("atualiza um insumo existente e recusa um inexistente")
    void atualizaInsumo() {
        assertTrue(arvore.atualizar("Tomate", new Insumo("Tomate", 20, "cx")));

        Insumo atualizado = arvore.buscar("Tomate");
        assertEquals(20, atualizado.getQuantidade());
        assertEquals("cx", atualizado.getUnidade());

        assertFalse(arvore.atualizar("Picanha", new Insumo("Picanha", 1, "kg")));
    }

    @Test
    @DisplayName("remove folha, nó com um filho e nó com dois filhos")
    void removeInsumos() {
        // folha
        assertTrue(arvore.remover("Alface"));
        assertNull(arvore.buscar("Alface"));

        // nó com um filho (Bacon ficou só com Cebola à direita)
        assertTrue(arvore.remover("Bacon"));
        assertNull(arvore.buscar("Bacon"));
        assertNotNull(arvore.buscar("Cebola"));

        // raiz com dois filhos: deve ser substituída pelo sucessor em-ordem
        assertTrue(arvore.remover("Queijo"));
        assertNull(arvore.buscar("Queijo"));
        assertNotNull(arvore.buscar("Cebola"));
        assertNotNull(arvore.buscar("Tomate"));
    }

    @Test
    @DisplayName("remover insumo inexistente devolve false sem alterar a árvore")
    void removeInsumoInexistente() {
        assertFalse(arvore.remover("Picanha"));
        assertNotNull(arvore.buscar("Queijo"));
    }
}
