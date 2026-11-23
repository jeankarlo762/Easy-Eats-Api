package com.easyeats.api.controller;

import com.easyeats.api.dto.CategoriaDto;
import com.easyeats.api.entity.Categoria;
import com.easyeats.api.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Categorias do cardápio (ex.: Lanches, Bebidas). Cada produto pertence a uma categoria.")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Criar
    @Operation(summary = "Cadastrar categoria", description = "Cadastra uma nova categoria do cardápio. Não aceita duas categorias com o mesmo nome.")
    @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (nome obrigatório)")
    @ApiResponse(responseCode = "422", description = "Já existe uma categoria com esse nome")
    @PostMapping
    public ResponseEntity<Categoria> criar(
            @Valid @RequestBody CategoriaDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.criar(dto));
    }

    // Listar
    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias cadastradas.")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    // Buscar por ID
    @Operation(summary = "Buscar categoria por ID", description = "Retorna os dados de uma categoria específica.")
    @ApiResponse(responseCode = "200", description = "Categoria encontrada")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    // Editar
    @Operation(summary = "Alterar categoria", description = "Altera os dados de uma categoria existente.")
    @ApiResponse(responseCode = "200", description = "Categoria alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @ApiResponse(responseCode = "422", description = "Já existe uma categoria com esse nome")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> editar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDto dto
    ) {
        return ResponseEntity.ok(
                categoriaService.editar(id, dto)
        );
    }

    // Inativar
    @Operation(summary = "Inativar categoria", description = "Marca a categoria como inativa, sem excluí-la do banco.")
    @ApiResponse(responseCode = "200", description = "Categoria inativada com sucesso")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Categoria> inativar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                categoriaService.inativar(id)
        );
    }
}
