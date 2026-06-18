package com.easy.eats.endereco.controller;

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

import com.easy.eats.endereco.model.Endereco;
import com.easy.eats.endereco.service.EnderecoService;

@Controller
@RequestMapping("/endereco")
public class EnderecoController {

    @Autowired
    EnderecoService service;

    @PostMapping
    public ResponseEntity<Endereco> criar(@RequestBody Endereco endereco) {
        Endereco novoEndereco = service.salvar(endereco);
        return ResponseEntity.ok(novoEndereco);
    }

    @GetMapping
    public List<Endereco> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Integer id) {
        Optional<Endereco> endereco = service.buscarPorId(id);
        return endereco.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endereco> atualizar(@PathVariable Integer id, @RequestBody Endereco enderecoAtualizado) {
        return service.buscarPorId(id).map(enderecoExistente -> {
            enderecoExistente.setRua(enderecoAtualizado.getRua());
            enderecoExistente.setNumero(enderecoAtualizado.getNumero());
            enderecoExistente.setBairro(enderecoAtualizado.getBairro());
            enderecoExistente.setCidade(enderecoAtualizado.getCidade());
            enderecoExistente.setCep(enderecoAtualizado.getCep());
            enderecoExistente.setComplemento(enderecoAtualizado.getComplemento());
            Endereco enderecoSalvo = service.salvar(enderecoExistente);
            return ResponseEntity.ok(enderecoSalvo);
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
