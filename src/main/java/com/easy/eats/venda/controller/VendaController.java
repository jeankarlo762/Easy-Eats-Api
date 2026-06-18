package com.easy.eats.venda.controller;

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

import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.service.VendaService;

@Controller
@RequestMapping("/venda")
public class VendaController {

    @Autowired
    VendaService service;

    @PostMapping
    public ResponseEntity<Venda> criar(@RequestBody Venda venda) {
        Venda novoVenda = service.salvar(venda);
        return ResponseEntity.ok(novoVenda);
    }

    @GetMapping
    public List<Venda> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Integer id) {
        Optional<Venda> venda = service.buscarPorId(id);
        return venda.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venda> atualizar(@PathVariable Integer id, @RequestBody Venda vendaAtualizado) {
        return service.buscarPorId(id).map(vendaExistente -> {
            vendaExistente.setStatus(vendaAtualizado.getStatus());
            vendaExistente.setTipo(vendaAtualizado.getTipo());
            Venda vendaSalvo = service.salvar(vendaExistente);
            return ResponseEntity.ok(vendaSalvo);
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
