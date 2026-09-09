package com.easy.eats.notificacao.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.notificacao.model.Notificacao;
import com.easy.eats.notificacao.service.NotificacaoService;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService service;

    @GetMapping
    public List<Notificacao> listar() {
        return service.listar();
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<Notificacao> marcarComoLida(@PathVariable Integer id) {
        return ResponseEntity.ok(service.marcarComoLida(id));
    }

    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas() {
        service.marcarTodasComoLidas();
        return ResponseEntity.noContent().build();
    }
}
