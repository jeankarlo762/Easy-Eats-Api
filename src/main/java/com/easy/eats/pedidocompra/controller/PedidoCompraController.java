package com.easy.eats.pedidocompra.controller;

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

import com.easy.eats.pedidocompra.model.PedidoCompra;
import com.easy.eats.pedidocompra.service.PedidoCompraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos-compra")
public class PedidoCompraController {

    @Autowired
    private PedidoCompraService service;

    @PostMapping
    public ResponseEntity<PedidoCompra> criar(@Valid @RequestBody PedidoCompra pedido) {
        return ResponseEntity.ok(service.criar(pedido));
    }

    @GetMapping
    public List<PedidoCompra> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoCompra> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/avancar")
    public ResponseEntity<PedidoCompra> avancarStatus(@PathVariable Integer id) {
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
