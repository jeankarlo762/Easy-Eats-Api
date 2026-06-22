package com.easy.eats.empresa.controller;

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

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.service.EmpresaService;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {
     private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Empresa> salvar(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(service.salvar(empresa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(
            @PathVariable Integer id,
            @RequestBody Empresa empresa) {

        return ResponseEntity.ok(service.atualizar(id, empresa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
}
