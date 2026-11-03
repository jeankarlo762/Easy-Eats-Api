package com.easy.eats.cardapiopublico.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.cardapiopublico.dto.CardapioPublicoResponse;
import com.easy.eats.cardapiopublico.dto.PedidoPublicoRequest;
import com.easy.eats.cardapiopublico.dto.PedidoPublicoResponse;
import com.easy.eats.cardapiopublico.service.CardapioPublicoService;

@RestController
public class CardapioPublicoController {

    @Autowired
    CardapioPublicoService service;

    /** Liberado sem JWT pelo matcher /public/** do SecurityConfig. */
    @GetMapping("/public/cardapio/{slug}")
    public ResponseEntity<CardapioPublicoResponse> buscarCardapio(
            @PathVariable String slug, @RequestParam(required = false) Integer mesa) {
        return ResponseEntity.ok(service.buscarCardapio(slug, mesa));
    }

    @PostMapping("/public/cardapio/{slug}/pedido")
    public ResponseEntity<PedidoPublicoResponse> criarPedido(
            @PathVariable String slug, @RequestBody PedidoPublicoRequest request) {
        return ResponseEntity.ok(service.criarPedido(slug, request));
    }

    /**
     * Autenticado (cai no anyRequest().authenticated()): qualquer perfil que
     * acessa o Novo Pedido precisa do slug para gerar o QR Code, inclusive
     * quem não pode chamar GET /empresa/minha-empresa.
     */
    @GetMapping("/cardapio-publico/meu-link")
    public ResponseEntity<Map<String, String>> meuLink() {
        return ResponseEntity.ok(Map.of("slug", service.slugDaMinhaEmpresa()));
    }
}
