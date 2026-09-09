package com.easy.eats.entregador.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.entregador.model.Entregador;
import com.easy.eats.entregador.service.EntregadorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/entregadores")
public class EntregadorController {

    @Autowired
    private EntregadorService service;

    @PostMapping
    public ResponseEntity<Entregador> criar(@Valid @RequestBody Entregador entregador) {
        return ResponseEntity.ok(service.criar(entregador));
    }

    @GetMapping
    public List<Entregador> listar() {
        return service.listarTodos();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entregador> atualizar(@PathVariable Integer id, @Valid @RequestBody Entregador entregador) {
        return ResponseEntity.ok(service.atualizar(id, entregador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
