package com.easy.eats.movimentacaoFinanceira.controller;

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

import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.service.MovimentacaoFinanceiraService;


@Controller
@RequestMapping("/movimentacao_financeira")
public class MovimentacaoFinanceiraController {

    @Autowired
    MovimentacaoFinanceiraService service;

    @PostMapping
    public ResponseEntity<MovimentacaoFinanceira> criar(@RequestBody MovimentacaoFinanceira movimentacao) {
        MovimentacaoFinanceira novaMovimentacao = service.salvar(movimentacao);
        return ResponseEntity.ok(novaMovimentacao);
    }

    @GetMapping
    public List<MovimentacaoFinanceira> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceira> buscarPorId(@PathVariable Integer id) {
        Optional<MovimentacaoFinanceira> movimentacao = service.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceira> atualizar(@PathVariable Integer id, @RequestBody MovimentacaoFinanceira movimentacaoAtualizada) {
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
