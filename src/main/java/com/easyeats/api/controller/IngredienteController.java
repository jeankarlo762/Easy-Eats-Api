package com.easyeats.api.controller;

import com.easyeats.api.dto.IngredienteDto;
import com.easyeats.api.service.IngredienteService;
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
@RequestMapping("/ingredientes")
@RequiredArgsConstructor
@Tag(name = "Ingredientes", description = "Ingredientes usados nas fichas técnicas dos produtos, com unidade de medida e custo.")
@SecurityRequirement(name = "bearerAuth")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @Operation(summary = "Cadastrar ingrediente", description = "Cadastra um novo ingrediente com unidade de medida e custo. Não aceita nomes repetidos.")
    @ApiResponse(responseCode = "201", description = "Ingrediente cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (nome, unidade e custo são obrigatórios)")
    @ApiResponse(responseCode = "422", description = "Já existe um ingrediente com esse nome")
    @PostMapping
    public ResponseEntity<IngredienteDto> criar(@Valid @RequestBody IngredienteDto ingrediente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.salvar(ingrediente));
    }

    @Operation(summary = "Listar ingredientes", description = "Retorna os ingredientes ativos.")
    @ApiResponse(responseCode = "200", description = "Lista de ingredientes retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<IngredienteDto>> listarTodos() {
        return ResponseEntity.ok(ingredienteService.listarTodos());
    }

    @Operation(summary = "Buscar ingrediente por ID", description = "Retorna os dados de um ingrediente ativo.")
    @ApiResponse(responseCode = "200", description = "Ingrediente encontrado")
    @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<IngredienteDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ingredienteService.buscarPorId(id));
    }

    @Operation(summary = "Buscar ingrediente por nome", description = "Retorna o ingrediente que tem o nome informado, sem diferenciar maiúsculas de minúsculas.")
    @ApiResponse(responseCode = "200", description = "Ingrediente encontrado")
    @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado")
    @GetMapping("/nome/{nome}")
    public ResponseEntity<IngredienteDto> buscarPorNome(@PathVariable String nome) {
        return ingredienteService.buscarPorNome(nome)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar ingrediente", description = "Altera os dados de um ingrediente existente.")
    @ApiResponse(responseCode = "200", description = "Ingrediente alterado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado")
    @ApiResponse(responseCode = "422", description = "Já existe um ingrediente com esse nome")
    @PutMapping("/{id}")
    public ResponseEntity<IngredienteDto> alterar(@PathVariable Long id,
                                                  @Valid @RequestBody IngredienteDto ingrediente) {
        return ResponseEntity.ok(ingredienteService.alterar(id, ingrediente));
    }

    @Operation(summary = "Excluir ingrediente", description = "Exclusão lógica: o ingrediente passa a inativo. Não é possível excluir um ingrediente usado por produto ativo.")
    @ApiResponse(responseCode = "204", description = "Ingrediente excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado")
    @ApiResponse(responseCode = "422", description = "Ingrediente em uso por produto ativo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        ingredienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
