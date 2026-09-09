package com.easy.eats.integracao.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.integracao.dto.IntegracaoConfigPayload;
import com.easy.eats.integracao.model.IntegracaoConfig;
import com.easy.eats.integracao.service.IntegracaoConfigService;

@RestController
@RequestMapping("/integracoes")
public class IntegracaoConfigController {

    @Autowired
    private IntegracaoConfigService service;

    @GetMapping
    public List<IntegracaoConfig> listar() {
        return service.listar();
    }

    @PutMapping("/{chave}")
    public ResponseEntity<IntegracaoConfig> salvar(@PathVariable String chave, @RequestBody IntegracaoConfigPayload payload) {
        return ResponseEntity.ok(service.salvar(chave, payload.credenciaisJson(), payload.ativo()));
    }
}
