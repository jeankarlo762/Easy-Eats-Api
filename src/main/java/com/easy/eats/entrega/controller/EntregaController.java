package com.easy.eats.entrega.controller;

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

import com.easy.eats.entrega.dto.RelatorioDelivery;
import com.easy.eats.entrega.model.Entrega;
import com.easy.eats.entrega.service.EntregaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    @Autowired
    private EntregaService service;

    @PostMapping
    public ResponseEntity<Entrega> criar(@Valid @RequestBody Entrega entrega) {
        return ResponseEntity.ok(service.criar(entrega));
    }

    @GetMapping
    public List<Entrega> listar() {
        return service.listarTodos();
    }

    @GetMapping("/relatorio")
    public RelatorioDelivery relatorio() {
        return service.relatorio();
    }

    @PutMapping("/{id}/avancar")
    public ResponseEntity<Entrega> avancarStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(service.avancarStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
