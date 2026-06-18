package com.easy.eats.mesa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.service.MesaService;


@Controller
@RequestMapping("/mesa")
public class MesaController {

    @Autowired
    MesaService service;

    @PostMapping
    public ResponseEntity<Mesa> criar(@RequestBody Mesa mesa) {
        Mesa novaMesa = service.salvar(mesa);
        return ResponseEntity.ok(novaMesa);
    }

    @GetMapping
    public List<Mesa> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mesa> buscarPorId(@PathVariable Integer id) {
        Optional<Mesa> mesa = service.buscarPorId(id);
        return mesa.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mesa> atualizar(@PathVariable Integer id, @RequestBody Mesa mesaAtualizado) {
        return service.buscarPorId(id).map(mesaExistente -> {
            mesaExistente.setStatus(mesaAtualizado.getStatus());
            mesaExistente.setNumero(mesaAtualizado.getNumero());
            Mesa mesaSalva = service.salvar(mesaExistente);
            return ResponseEntity.ok(mesaSalva);
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
