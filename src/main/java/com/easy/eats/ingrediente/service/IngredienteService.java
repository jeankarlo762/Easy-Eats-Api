package com.easy.eats.ingrediente.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.categoria.model.Categoria;
import com.easy.eats.estoque.enums.TipoMovimentacaoEstoque;
import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.estoque.repository.EstoqueRepository;
import com.easy.eats.estoque.service.EstoqueService;
import com.easy.eats.ingrediente.dto.IngredientePayload;
import com.easy.eats.ingrediente.dto.IngredienteResponse;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.service.ProdutoService;

/**
 * Cadastro de ingredientes sobre a modelagem já existente: um ingrediente é um
 * Produto de natureza INSUMO com o seu registro de Estoque. Não cria tabela
 * nova — pedidos de compra, movimentações e a ficha técnica continuam
 * referenciando o mesmo produto_id.
 */
@Service
public class IngredienteService {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private EstoqueRepository estoqueRepository;

    public List<IngredienteResponse> listar() {
        Map<Integer, Estoque> estoquePorProduto = estoqueService.listarTodos().stream()
                .collect(Collectors.toMap(e -> e.getProduto().getId(), Function.identity()));

        return produtoService.listarTodos().stream()
                .filter(this::ehIngredienteAtivo)
                .map(produto -> IngredienteResponse.de(produto, estoquePorProduto.get(produto.getId())))
                .toList();
    }

    public IngredienteResponse buscarPorId(Integer id) {
        Produto produto = ingrediente(id);
        return IngredienteResponse.de(produto, estoqueRepository.findByProdutoId(id).orElse(null));
    }

    @Transactional
    public IngredienteResponse criar(IngredientePayload payload) {
        Produto produto = new Produto();
        produto.setNome(payload.nome().trim());
        produto.setUnidadeMedida(payload.unidadeMedida());
        produto.setCusto(payload.custo());
        // Ingrediente não é vendido: o preço (obrigatório em Produto) fica zerado.
        produto.setPreco(BigDecimal.ZERO);
        produto.setNatureza(NaturezaProduto.INSUMO);
        produto.setFlAtivo(true);
        produto.setCategoria(categoria(payload.categoriaId()));

        Produto salvo = produtoService.salvar(produto);
        Estoque estoque = estoqueService.criar(salvo.getId(), payload.quantidadeEstoque(), payload.estoqueMinimo());

        return IngredienteResponse.de(salvo, estoque);
    }

    /**
     * A diferença de quantidade em estoque é registrada como movimentação de
     * ajuste (entrada ou saída), para o saldo continuar batendo com o histórico.
     */
    @Transactional
    public IngredienteResponse atualizar(Integer id, IngredientePayload payload) {
        Produto existente = ingrediente(id);

        Produto alterado = new Produto();
        alterado.setNome(payload.nome().trim());
        alterado.setDescricao(existente.getDescricao());
        alterado.setPreco(existente.getPreco() != null ? existente.getPreco() : BigDecimal.ZERO);
        alterado.setCusto(payload.custo());
        alterado.setFlAtivo(true);
        alterado.setNatureza(NaturezaProduto.INSUMO);
        alterado.setUnidadeMedida(payload.unidadeMedida());
        alterado.setCategoria(categoria(payload.categoriaId()));
        Produto salvo = produtoService.atualizar(id, alterado);

        Estoque estoque = estoqueRepository.findByProdutoId(id).orElse(null);
        if (estoque == null) {
            estoque = estoqueService.criar(id, payload.quantidadeEstoque(), payload.estoqueMinimo());
        } else {
            int quantidadeAtual = estoque.getQuantidadeAtual() != null ? estoque.getQuantidadeAtual() : 0;
            estoqueService.atualizar(id, quantidadeAtual, payload.estoqueMinimo());

            int diferenca = payload.quantidadeEstoque() - quantidadeAtual;
            if (diferenca != 0) {
                estoqueService.registrarMovimentacao(id,
                        diferenca > 0 ? TipoMovimentacaoEstoque.ENTRADA : TipoMovimentacaoEstoque.SAIDA,
                        Math.abs(diferenca), "Ajuste pelo cadastro de ingredientes");
            }
            estoque = estoqueService.buscarPorProduto(id);
        }

        return IngredienteResponse.de(salvo, estoque);
    }

    /**
     * Bloqueia se o ingrediente ainda compõe a ficha técnica de algum produto
     * ativo (validação em ProdutoService.deletar). O produto é desativado, não
     * apagado — movimentações e pedidos de compra continuam apontando para ele —
     * e o registro de estoque sai do controle de estoque.
     */
    @Transactional
    public void excluir(Integer id) {
        ingrediente(id);
        produtoService.deletar(id);
        estoqueRepository.findByProdutoId(id).ifPresent(estoqueRepository::delete);
    }

    private Produto ingrediente(Integer id) {
        Produto produto = produtoService.buscarPorId(id);
        if (!ehIngredienteAtivo(produto)) {
            throw new RuntimeException("Ingrediente não encontrado");
        }
        return produto;
    }

    private boolean ehIngredienteAtivo(Produto produto) {
        return produto.getNatureza() == NaturezaProduto.INSUMO && !Boolean.FALSE.equals(produto.getFlAtivo());
    }

    private Categoria categoria(Integer categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        return categoria;
    }
}
