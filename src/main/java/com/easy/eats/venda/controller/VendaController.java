package com.easy.eats.venda.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easy.eats.venda.model.RelatorioFaturamento;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.quicksort.ProdutoRanking;
import com.easy.eats.venda.service.VendaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/venda")
public class VendaController {

    @Autowired
    VendaService service;

    @PostMapping
    public ResponseEntity<Venda> criar(@Valid @RequestBody Venda venda) {
        Venda novoVenda = service.criar(venda);
        return ResponseEntity.ok(novoVenda);
    }

    @GetMapping
    public List<Venda> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Integer id) {
        Optional<Venda> venda = service.buscarPorId(id);
        return venda.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venda> atualizar(@PathVariable Integer id, @Valid @RequestBody Venda vendaAtualizado) {
        return service.buscarPorId(id)
                .map(vendaExistente -> ResponseEntity
                        .ok(service.atualizarStatus(vendaExistente, vendaAtualizado.getStatus(), vendaAtualizado.getTipo())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ProdutoRanking>> rankingProdutos() {
        return ResponseEntity.ok(service.rankingProdutos());
    }

    /** Histórico de compras de um cliente cadastrado — base para fidelidade e cashback. */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Venda>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @PostMapping("/{id}/cupom")
    public ResponseEntity<Venda> aplicarCupom(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.aplicarCupom(id, body.get("codigo")));
    }

    @PostMapping("/{id}/resgatar-cashback")
    public ResponseEntity<Venda> resgatarCashback(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Object valor = body.get("valor");
        return ResponseEntity.ok(service.resgatarCashback(id, valor == null ? null : new BigDecimal(valor.toString())));
    }

    /** Faturamento agregado por período (padrão: últimos 30 dias). */
    @GetMapping("/relatorio")
    public ResponseEntity<RelatorioFaturamento> relatorio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        LocalDate fimEfetivo = fim != null ? fim : LocalDate.now();
        LocalDate inicioEfetivo = inicio != null ? inicio : fimEfetivo.minusDays(30);
        return ResponseEntity.ok(service.relatorioFaturamento(
                inicioEfetivo.atStartOfDay(), LocalDateTime.of(fimEfetivo, LocalTime.MAX)));
    }

}
