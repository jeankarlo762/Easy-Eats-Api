package com.easyeats.api.controller;

import com.easyeats.api.dto.EstoqueDto;
import com.easyeats.api.dto.MovimentacaoDto;
import com.easyeats.api.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/estoques")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Controle do estoque de ingredientes. O estoque é baixado quando a comanda entra em preparo, de acordo com a ficha técnica dos produtos.")
@SecurityRequirement(name = "bearerAuth")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @Operation(summary = "Cadastrar estoque de um ingrediente", description = "Cria o estoque de um ingrediente ativo. Cada ingrediente tem um único estoque.")
    @ApiResponse(responseCode = "201", description = "Estoque cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (quantidades obrigatórias, até 3 casas decimais)")
    @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado")
    @ApiResponse(responseCode = "422", description = "Este ingrediente já possui estoque cadastrado")
    @PostMapping
    public ResponseEntity<EstoqueDto> criar(@Valid @RequestBody EstoqueDto estoque) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.salvar(estoque));
    }

    @Operation(summary = "Listar estoques", description = "Retorna o estoque de todos os ingredientes ativos.")
    @ApiResponse(responseCode = "200", description = "Lista de estoques retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<EstoqueDto>> listarTodos() {
        return ResponseEntity.ok(estoqueService.listarTodos());
    }

    @Operation(summary = "Listar alertas de estoque baixo", description = "Retorna os estoques cuja quantidade atual está igual ou abaixo da quantidade mínima.")
    @ApiResponse(responseCode = "200", description = "Lista de estoques em alerta retornada com sucesso")
    @GetMapping("/alertas")
    public ResponseEntity<List<EstoqueDto>> listarAlertas() {
        return ResponseEntity.ok(estoqueService.listarAlertas());
    }

    @Operation(summary = "Buscar estoque por ID", description = "Retorna os dados de um estoque específico.")
    @ApiResponse(responseCode = "200", description = "Estoque encontrado")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estoqueService.buscarPorId(id));
    }

    @Operation(summary = "Buscar estoque por ingrediente", description = "Retorna o estoque do ingrediente informado.")
    @ApiResponse(responseCode = "200", description = "Estoque encontrado")
    @ApiResponse(responseCode = "404", description = "Estoque do ingrediente não encontrado")
    @GetMapping("/ingrediente/{idIngrediente}")
    public ResponseEntity<EstoqueDto> buscarPorIngrediente(@PathVariable Long idIngrediente) {
        return ResponseEntity.ok(estoqueService.buscarPorIngrediente(idIngrediente));
    }

    @Operation(summary = "Alterar quantidade mínima do estoque", description = "Altera somente a quantidade mínima. A quantidade atual só muda por entrada ou saída, e o ingrediente do estoque não pode ser trocado.")
    @ApiResponse(responseCode = "200", description = "Estoque alterado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @ApiResponse(responseCode = "409", description = "O estoque foi alterado por outra operação ao mesmo tempo. Tente novamente")
    @ApiResponse(responseCode = "422", description = "Tentativa de trocar o ingrediente ou de alterar a quantidade atual")
    @PutMapping("/{id}")
    public ResponseEntity<EstoqueDto> alterar(@PathVariable Long id,
            @Valid @RequestBody EstoqueDto estoque) {
        return ResponseEntity.ok(estoqueService.alterar(id, estoque));
    }

    @Operation(summary = "Registrar entrada de estoque", description = "Soma a quantidade informada ao estoque atual (ex.: compra de mercadoria).")
    @ApiResponse(responseCode = "200", description = "Entrada registrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Quantidade inválida (deve ser maior que zero)")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @ApiResponse(responseCode = "409", description = "O estoque foi alterado por outra operação ao mesmo tempo. Tente novamente")
    @PatchMapping("/{id}/entrada")
    public ResponseEntity<EstoqueDto> entrada(@PathVariable Long id,
            @Valid @RequestBody MovimentacaoDto mov) {
        return ResponseEntity.ok(estoqueService.registrarEntrada(id, mov.quantidade()));
    }

    @Operation(summary = "Registrar saída de estoque", description = "Subtrai a quantidade informada do estoque atual (ex.: perda ou descarte). Não permite saída maior que o saldo.")
    @ApiResponse(responseCode = "200", description = "Saída registrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Quantidade inválida (deve ser maior que zero)")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @ApiResponse(responseCode = "409", description = "O estoque foi alterado por outra operação ao mesmo tempo. Tente novamente")
    @ApiResponse(responseCode = "422", description = "Estoque insuficiente")
    @PatchMapping("/{id}/saida")
    public ResponseEntity<EstoqueDto> saida(@PathVariable Long id,
            @Valid @RequestBody MovimentacaoDto mov) {
        return ResponseEntity.ok(estoqueService.registrarSaida(id, mov.quantidade()));
    }

    @Operation(summary = "Excluir estoque", description = "Exclui o estoque de um ingrediente. Não é possível se o ingrediente é usado por produto ativo.")
    @ApiResponse(responseCode = "204", description = "Estoque excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @ApiResponse(responseCode = "422", description = "Ingrediente em uso por produto ativo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        estoqueService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar produtos sem estoque", description = "Retorna os IDs dos produtos ativos que não têm estoque suficiente de algum ingrediente para serem preparados.")
    @ApiResponse(responseCode = "200", description = "Lista de IDs de produtos retornada com sucesso")
    @GetMapping("/produtos-sem-estoque")
    public ResponseEntity<List<Long>> listarProdutosSemEstoque() {
        return ResponseEntity.ok(estoqueService.listarProdutosSemEstoque());
    }
}
