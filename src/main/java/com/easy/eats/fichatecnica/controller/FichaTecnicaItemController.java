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
import com.easy.eats.fichatecnica.dto.FichaTecnicaItemResponse;
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
    public List<FichaTecnicaItemResponse> listar(@PathVariable Integer produtoId) {
        return service.listarPorProduto(produtoId).stream().map(FichaTecnicaItemResponse::de).toList();
    }

    @GetMapping("/custo-total")
    public Map<String, BigDecimal> custoTotal(@PathVariable Integer produtoId) {
        return Map.of("custoTotal", service.custoTotal(produtoId));
    }

    /** Substitui a composição inteira do produto (salvar a ficha técnica junto com o cadastro do produto). */
    @PutMapping
    public List<FichaTecnicaItemResponse> substituir(
            @PathVariable Integer produtoId,
            @RequestBody List<FichaTecnicaItemPayload> itens) {
        return service.substituir(produtoId, itens).stream().map(FichaTecnicaItemResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<FichaTecnicaItemResponse> salvar(
            @PathVariable Integer produtoId,
            @Valid @RequestBody FichaTecnicaItemPayload payload) {
        return ResponseEntity.ok(FichaTecnicaItemResponse.de(
                service.salvar(produtoId, payload.ingredienteId(), payload.quantidadeUtilizada())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FichaTecnicaItemResponse> atualizar(
            @PathVariable Integer produtoId,
            @PathVariable Integer id,
            @Valid @RequestBody FichaTecnicaItemPayload payload) {
        return ResponseEntity.ok(FichaTecnicaItemResponse.de(
                service.atualizar(produtoId, id, payload.quantidadeUtilizada())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer produtoId, @PathVariable Integer id) {
        service.deletar(produtoId, id);
        return ResponseEntity.noContent().build();
    }
}
