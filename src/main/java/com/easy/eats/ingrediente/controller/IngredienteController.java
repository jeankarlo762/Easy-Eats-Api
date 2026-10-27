package com.easy.eats.ingrediente.controller;

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

import com.easy.eats.ingrediente.dto.IngredientePayload;
import com.easy.eats.ingrediente.dto.IngredienteResponse;
import com.easy.eats.ingrediente.service.IngredienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ingredientes")
public class IngredienteController {

    private final IngredienteService service;

    public IngredienteController(IngredienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<IngredienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public IngredienteResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public IngredienteResponse criar(@Valid @RequestBody IngredientePayload payload) {
        return service.criar(payload);
    }

    @PutMapping("/{id}")
    public IngredienteResponse atualizar(@PathVariable Integer id, @Valid @RequestBody IngredientePayload payload) {
        return service.atualizar(id, payload);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
