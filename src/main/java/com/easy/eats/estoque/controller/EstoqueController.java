package com.easy.eats.estoque.controller;

import java.util.List;
import java.util.Map;

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

import com.easy.eats.estoque.enums.TipoMovimentacaoEstoque;
import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.estoque.model.ItemMaisConsumido;
import com.easy.eats.estoque.model.MovimentacaoEstoque;
import com.easy.eats.estoque.service.EstoqueService;

/**
 * Nível de estoque por produto (tipicamente produtos de natureza INSUMO).
 * Substituiu uma implementação anterior que guardava tudo em uma árvore
 * binária em memória, sem persistência nem isolamento por empresa.
 */
@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private EstoqueService service;

    @GetMapping
    public List<Estoque> listar() {
        return service.listarTodos();
    }

    @GetMapping("/abaixo-do-minimo")
    public List<Estoque> listarAbaixoDoMinimo() {
        return service.listarAbaixoDoMinimo();
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<Estoque> buscarPorProduto(@PathVariable Integer produtoId) {
        return ResponseEntity.ok(service.buscarPorProduto(produtoId));
    }

    @PostMapping
    public ResponseEntity<Estoque> criar(@RequestBody Map<String, Object> body) {
        Integer produtoId = idDoProduto(body);
        Integer quantidadeAtual = (Integer) body.get("quantidadeAtual");
        Integer estoqueMinimo = (Integer) body.get("estoqueMinimo");
        return ResponseEntity.ok(service.criar(produtoId, quantidadeAtual, estoqueMinimo));
    }

    @PutMapping("/{produtoId}")
    public ResponseEntity<Estoque> atualizar(@PathVariable Integer produtoId, @RequestBody Map<String, Object> body) {
        Integer quantidadeAtual = (Integer) body.get("quantidadeAtual");
        Integer estoqueMinimo = (Integer) body.get("estoqueMinimo");
        return ResponseEntity.ok(service.atualizar(produtoId, quantidadeAtual, estoqueMinimo));
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<Void> remover(@PathVariable Integer produtoId) {
        service.remover(produtoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return service.listarMovimentacoesRecentes();
    }

    @GetMapping("/mais-consumidos")
    public List<ItemMaisConsumido> listarMaisConsumidos() {
        return service.listarMaisConsumidos();
    }

    @PostMapping("/{produtoId}/movimentacao")
    public ResponseEntity<MovimentacaoEstoque> registrarMovimentacao(@PathVariable Integer produtoId,
            @RequestBody Map<String, Object> body) {
        TipoMovimentacaoEstoque tipo = TipoMovimentacaoEstoque.valueOf(((String) body.get("tipo")).toUpperCase());
        Integer quantidade = (Integer) body.get("quantidade");
        String observacao = (String) body.get("observacao");
        return ResponseEntity.ok(service.registrarMovimentacao(produtoId, tipo, quantidade, observacao));
    }

    private Integer idDoProduto(Map<String, Object> body) {
        Object produto = body.get("produto");
        if (produto instanceof Map<?, ?> mapa) {
            Object id = mapa.get("id");
            return id != null ? (Integer) id : null;
        }
        return null;
    }
}
