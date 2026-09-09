package com.easy.eats.caixa.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.caixa.service.CaixaService;
import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;

@RestController
@RequestMapping("/caixa")
public class CaixaController {

    @Autowired
    CaixaService service;

    @GetMapping("/status")
    public ResponseEntity<Caixa> status() {
        Caixa caixa = service.status();
        return caixa != null ? ResponseEntity.ok(caixa) : ResponseEntity.noContent().build();
    }

    @PostMapping("/abrir")
    public ResponseEntity<Caixa> abrir(@RequestBody Map<String, Object> body) {
        BigDecimal valorInicial = numeroDoBody(body.get("valorInicial"));
        String observacoes = (String) body.get("observacoes");
        return ResponseEntity.ok(service.abrir(valorInicial, observacoes));
    }

    @PutMapping("/{id}/fechar")
    public ResponseEntity<Caixa> fechar(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BigDecimal valorApuradoInformado = numeroDoBody(body.get("valorApuradoInformado"));
        String observacoes = (String) body.get("observacoes");
        return ResponseEntity.ok(service.fechar(id, valorApuradoInformado, observacoes));
    }

    @PostMapping("/{id}/movimentacao")
    public ResponseEntity<MovimentacaoFinanceira> movimentacao(@PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        String tipo = (String) body.get("tipo");
        BigDecimal valor = numeroDoBody(body.get("valor"));
        String descricao = (String) body.get("descricao");
        return ResponseEntity.ok(service.registrarMovimentacao(id, tipo, valor, descricao));
    }

    /**
     * O corpo chega como Map genérico (não um DTO tipado), então o valor
     * numérico vem como Integer/Double conforme o JSON recebido. Construir o
     * BigDecimal a partir do texto (via toString) evita herdar a imprecisão
     * binária de um double intermediário — new BigDecimal(número.doubleValue())
     * arrastaria o mesmo erro de ponto flutuante que motivou esta migração.
     */
    private BigDecimal numeroDoBody(Object valor) {
        return valor == null ? null : new BigDecimal(valor.toString());
    }
}
