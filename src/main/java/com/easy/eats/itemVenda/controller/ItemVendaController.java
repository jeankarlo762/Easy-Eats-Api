package com.easy.eats.itemVenda.controller;

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

import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.service.ItemVendaService;

import com.easy.eats.venda.service.VendaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/item-venda")
public class ItemVendaController {

    @Autowired
    ItemVendaService service;

    @Autowired
    VendaService vendaService;

    @PostMapping
    public ResponseEntity<ItemVenda> criar(@Valid @RequestBody ItemVenda itemVenda) {
        ItemVenda novoItemVenda = service.criar(itemVenda);
        vendaService.recalcularTotal(novoItemVenda.getVenda().getId());
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
    public ResponseEntity<ItemVenda> atualizar(@PathVariable Integer id, @Valid @RequestBody ItemVenda itemVendaAtualizado) {
        return service.buscarPorId(id).map(itemVendaExistente -> {
            itemVendaExistente.setQuantidade(itemVendaAtualizado.getQuantidade());
            itemVendaExistente.setPreco_unitario(itemVendaAtualizado.getPreco_unitario());
            itemVendaExistente.setCusto_unitario(itemVendaAtualizado.getCusto_unitario());
            itemVendaExistente.setValor_total(itemVendaAtualizado.getValor_total());
            itemVendaExistente.setDesconto(itemVendaAtualizado.getDesconto());
            itemVendaExistente.setObservacao(itemVendaAtualizado.getObservacao());
            ItemVenda itemVendaSalvo = service.salvar(itemVendaExistente);
            vendaService.recalcularTotal(itemVendaSalvo.getVenda().getId());
            return ResponseEntity.ok(itemVendaSalvo);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        return service.buscarPorId(id).map(itemVendaExistente -> {
            Integer vendaId = itemVendaExistente.getVenda().getId();
            service.deletar(id);
            vendaService.recalcularTotal(vendaId);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}
