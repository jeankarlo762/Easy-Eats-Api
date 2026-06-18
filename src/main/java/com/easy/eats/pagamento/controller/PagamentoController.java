package com.easy.eats.pagamento.controller;

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

import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.pagamento.service.PagamentoService;

@Controller
@RequestMapping("/pagamento")
public class PagamentoController {

    @Autowired
    PagamentoService service;

    @PostMapping
    public ResponseEntity<Pagamento> criar(@RequestBody Pagamento pagamento) {
        Pagamento novoPagamento = service.salvar(pagamento);
        return ResponseEntity.ok(novoPagamento);
    }

    @GetMapping
    public List<Pagamento> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscarPorId(@PathVariable Integer id) {
        Optional<Pagamento> pagamento = service.buscarPorId(id);
        return pagamento.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pagamento> atualizar(@PathVariable Integer id, @RequestBody Pagamento pagamentoAtualizado) {
        return service.buscarPorId(id).map(pagamentoExistente -> {
            pagamentoExistente.setMetodo(pagamentoAtualizado.getMetodo());
            pagamentoExistente.setValor(pagamentoAtualizado.getValor());
            pagamentoExistente.setStatus(pagamentoAtualizado.getStatus());
            pagamentoExistente.setDt_pagamento(pagamentoAtualizado.getDt_pagamento());
            Pagamento pagamentoSalvo = service.salvar(pagamentoExistente);
            return ResponseEntity.ok(pagamentoSalvo);
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
