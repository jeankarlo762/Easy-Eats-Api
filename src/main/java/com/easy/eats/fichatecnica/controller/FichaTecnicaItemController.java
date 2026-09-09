package com.easy.eats.fichatecnica.controller;

import java.math.BigDecimal;
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

import com.easy.eats.fichatecnica.dto.FichaTecnicaItemPayload;
import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.fichatecnica.service.FichaTecnicaItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos/{produtoId}/ficha-tecnica")
public class FichaTecnicaItemController {

    private final FichaTecnicaItemService service;

    public FichaTecnicaItemController(FichaTecnicaItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<FichaTecnicaItem> listar(@PathVariable Integer produtoId) {
        return service.listarPorProduto(produtoId);
    }

    @GetMapping("/custo-total")
    public Map<String, BigDecimal> custoTotal(@PathVariable Integer produtoId) {
        return Map.of("custoTotal", service.custoTotal(produtoId));
    }

    @PostMapping
    public ResponseEntity<FichaTecnicaItem> salvar(
            @PathVariable Integer produtoId,
            @Valid @RequestBody FichaTecnicaItemPayload payload) {
        return ResponseEntity.ok(service.salvar(produtoId, payload.insumoId(), payload.quantidade()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FichaTecnicaItem> atualizar(
            @PathVariable Integer produtoId,
            @PathVariable Integer id,
            @Valid @RequestBody FichaTecnicaItemPayload payload) {
        return ResponseEntity.ok(service.atualizar(produtoId, id, payload.quantidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer produtoId, @PathVariable Integer id) {
        service.deletar(produtoId, id);
        return ResponseEntity.noContent().build();
    }
}
