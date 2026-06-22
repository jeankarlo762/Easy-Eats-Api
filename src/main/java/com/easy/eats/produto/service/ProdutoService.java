package com.easy.eats.produto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository){
        this.repository = repository;
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Produto salvar(Produto produto) {
        produto.setDtAlteracao(LocalDateTime.now());
        return repository.save(produto);
    }

    public Produto atualizar(Integer id, Produto produto) {

        Produto existente = buscarPorId(id);

        existente.setNome(produto.getNome());
        existente.setDescricao(produto.getDescricao());
        existente.setPreco(produto.getPreco());
        existente.setCusto(produto.getCusto());
        existente.setFlAtivo(produto.getFlAtivo());
        existente.setDtAlteracao(LocalDateTime.now());

        return repository.save(existente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    
}
