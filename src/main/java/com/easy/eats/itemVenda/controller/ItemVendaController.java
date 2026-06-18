package com.easy.eats.itemVenda.controller;

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

import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.service.ItemVendaService;

@Controller
@RequestMapping("/item-venda")
public class ItemVendaController {

    @Autowired
    ItemVendaService service;

    @PostMapping
    public ResponseEntity<ItemVenda> criar(@RequestBody ItemVenda itemVenda) {
        ItemVenda novoItemVenda = service.salvar(itemVenda);
        return ResponseEntity.ok(novoItemVenda);
    }

    @GetMapping
    public List<ItemVenda> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemVenda> buscarPorId(@PathVariable Integer id) {
        Optional<ItemVenda> itemVenda = service.buscarPorId(id);
        return itemVenda.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemVenda> atualizar(@PathVariable Integer id, @RequestBody ItemVenda itemVendaAtualizado) {
        return service.buscarPorId(id).map(itemVendaExistente -> {
            itemVendaExistente.setQuantidade(itemVendaAtualizado.getQuantidade());
            itemVendaExistente.setPreco_unitario(itemVendaAtualizado.getPreco_unitario());
            itemVendaExistente.setCusto_unitario(itemVendaAtualizado.getCusto_unitario());
            itemVendaExistente.setValor_total(itemVendaAtualizado.getValor_total());
            itemVendaExistente.setDesconto(itemVendaAtualizado.getDesconto());
            ItemVenda itemVendaSalvo = service.salvar(itemVendaExistente);
            return ResponseEntity.ok(itemVendaSalvo);
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
