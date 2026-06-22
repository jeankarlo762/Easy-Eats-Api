package com.easy.eats.cardapio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.cardapio.model.Cardapio;
import com.easy.eats.cardapio.service.CardapioService;

@RestController
@RequestMapping("/cardapio")
public class CardapioController {
    private final CardapioService service;

    public CardapioController(CardapioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cardapio>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cardapio> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Cardapio> salvar(@RequestBody Cardapio cardapio) {
        return ResponseEntity.ok(service.salvar(cardapio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cardapio> atualizar(
            @PathVariable Integer id,
            @RequestBody Cardapio cardapio) {

        return ResponseEntity.ok(service.atualizar(id, cardapio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
}
