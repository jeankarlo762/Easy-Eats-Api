package com.easy.eats.produto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.service.ProdutoService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;
    
    public ProdutoController(ProdutoService service){
        this.service = service;
    }
    @GetMapping
    public List<Produto> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Produto salvar(@RequestBody Produto produto) {
        return service.salvar(produto);
    }

    @PutMapping("/{id}")
    public Produto atualizar(
            @PathVariable Integer id,
            @RequestBody Produto produto) {

        return service.atualizar(id, produto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }


    
}
