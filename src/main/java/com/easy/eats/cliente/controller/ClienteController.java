package com.easy.eats.cliente.controller;

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

import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.cliente.service.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    ClienteService service;

    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente cliente) {
        Cliente novoCliente = service.criar(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    @GetMapping
    public List<Cliente> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Integer id) {
        Optional<Cliente> cliente = service.buscarPorId(id);
        return cliente.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Integer id, @Valid @RequestBody Cliente clienteAtualizado) {
        return service.buscarPorId(id).map(clienteExistente -> {
            clienteExistente.setNome(clienteAtualizado.getNome());
            clienteExistente.setEmail(clienteAtualizado.getEmail());
            clienteExistente.setTelefone(clienteAtualizado.getTelefone());
            clienteExistente.setCpf(clienteAtualizado.getCpf());
            clienteExistente.setEndereco(clienteAtualizado.getEndereco());
            Cliente clienteSalvo = service.salvar(clienteExistente);
            return ResponseEntity.ok(clienteSalvo);
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
