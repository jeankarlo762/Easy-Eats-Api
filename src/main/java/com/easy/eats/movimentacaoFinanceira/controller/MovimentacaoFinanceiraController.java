package com.easy.eats.movimentacaoFinanceira.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.service.MovimentacaoFinanceiraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimentacao_financeira")
public class MovimentacaoFinanceiraController {

    @Autowired
    MovimentacaoFinanceiraService service;

    @PostMapping
    public ResponseEntity<MovimentacaoFinanceira> criar(@Valid @RequestBody MovimentacaoFinanceira movimentacao) {
        MovimentacaoFinanceira novaMovimentacao = service.criar(movimentacao);
        return ResponseEntity.ok(novaMovimentacao);
    }

    @GetMapping
    public List<MovimentacaoFinanceira> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        // Sem inicio/fim: lista tudo (comportamento antigo). Com os dois: filtra por período.
        if (inicio == null || fim == null) {
            return service.listarTodos();
        }
        return service.listarPorPeriodo(inicio.atStartOfDay(), LocalDateTime.of(fim, LocalTime.MAX));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceira> buscarPorId(@PathVariable Integer id) {
        Optional<MovimentacaoFinanceira> movimentacao = service.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceira> atualizar(@PathVariable Integer id, @Valid @RequestBody MovimentacaoFinanceira movimentacaoAtualizada) {
        return service.buscarPorId(id).map(movimentacaoExistente -> {
            movimentacaoExistente.setTipo(movimentacaoAtualizada.getTipo());
            movimentacaoExistente.setCategoria(movimentacaoAtualizada.getCategoria());
            movimentacaoExistente.setValor(movimentacaoAtualizada.getValor());
            movimentacaoExistente.setDescricao(movimentacaoAtualizada.getDescricao());
            MovimentacaoFinanceira movimentacaoSalva = service.salvar(movimentacaoExistente);
            return ResponseEntity.ok(movimentacaoSalva);
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
