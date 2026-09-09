package com.easy.eats.despesa.controller;

import java.util.List;
import java.util.Optional;

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

import com.easy.eats.despesa.model.Despesa;
import com.easy.eats.despesa.service.DespesaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/despesa")
public class DespesaController {

    @Autowired
    private DespesaService service;

    @PostMapping
    public ResponseEntity<Despesa> criar(@Valid @RequestBody Despesa despesa) {
        return ResponseEntity.ok(service.criar(despesa));
    }

    @GetMapping
    public List<Despesa> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Despesa> buscarPorId(@PathVariable Integer id) {
        Optional<Despesa> despesa = service.buscarPorId(id);
        return despesa.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Despesa> atualizar(@PathVariable Integer id, @Valid @RequestBody Despesa despesaAtualizada) {
        return service.buscarPorId(id).map(despesaExistente -> {
            despesaExistente.setDescricao(despesaAtualizada.getDescricao());
            despesaExistente.setCategoria(despesaAtualizada.getCategoria());
            despesaExistente.setValor(despesaAtualizada.getValor());
            despesaExistente.setDtDespesa(despesaAtualizada.getDtDespesa());
            return ResponseEntity.ok(service.salvar(despesaExistente));
        }).orElseGet(() -> ResponseEntity.notFound().build());
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
