package com.easy.eats.fornecedor.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.easy.eats.fornecedor.model.Fornecedor;
import com.easy.eats.fornecedor.service.FornecedorService;

@RestController
@RequestMapping("/fornecedores")
@CrossOrigin(origins = "http://localhost:4200")
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Fornecedor> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Fornecedor buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Fornecedor salvar(@RequestBody Fornecedor fornecedor) {
        return service.salvar(fornecedor);
    }

    @PutMapping("/{id}")
    public Fornecedor atualizar(
            @PathVariable Integer id,
            @RequestBody Fornecedor fornecedor) {

        return service.atualizar(id, fornecedor);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }
}