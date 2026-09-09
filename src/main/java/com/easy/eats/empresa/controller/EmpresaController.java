package com.easy.eats.empresa.controller;

import java.util.List;
import java.util.Map;

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

import jakarta.validation.Valid;

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
    public ResponseEntity<Empresa> salvar(@Valid @RequestBody Empresa empresa) {
        return ResponseEntity.ok(service.salvar(empresa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Empresa empresa) {

        return ResponseEntity.ok(service.atualizar(id, empresa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/slug")
    public ResponseEntity<Empresa> atualizarSlug(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.atualizarSlug(id, body.get("slug")));
    }

    @GetMapping("/minha-empresa")
    public ResponseEntity<Empresa> buscarMinhaEmpresa() {
        return ResponseEntity.ok(service.buscarMinhaEmpresa());
    }

    @PutMapping("/minha-empresa")
    public ResponseEntity<Empresa> atualizarMinhaEmpresa(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(service.atualizarMinhaEmpresa(empresa));
    }

}
