package com.easyeats.api.controller;

import com.easyeats.api.dto.ProdutoIngredienteDto;
import com.easyeats.api.dto.ProdutoIngredienteLoteDto;
import com.easyeats.api.service.ProdutoIngredienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/produto-ingrediente")
@RequiredArgsConstructor
@Tag(name = "Ficha técnica", description = "Ingredientes e quantidades usados em cada produto. A ficha técnica define quanto estoque cada pedido consome.")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoIngredienteController {

    private final ProdutoIngredienteService service;

    @Operation(summary = "Salvar ficha técnica do produto", description = "Informa, de uma só vez, os ingredientes e as quantidades usados por um produto.")
    @ApiResponse(responseCode = "201", description = "Ficha técnica salva com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (informe ao menos um ingrediente)")
    @ApiResponse(responseCode = "404", description = "Produto ou ingrediente não encontrado")
    @ApiResponse(responseCode = "422", description = "Regra de negócio não atendida")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoIngredienteLoteDto salvar(
            @Valid
            @RequestBody ProdutoIngredienteLoteDto dto) {

        return service.salvar(dto);
    }

    @Operation(summary = "Listar fichas técnicas", description = "Retorna todos os vínculos entre produtos e ingredientes.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public List<ProdutoIngredienteDto> listar() {
        return service.listar();
    }

    @Operation(summary = "Buscar ingrediente de um produto", description = "Retorna a quantidade de um ingrediente na ficha técnica de um produto.")
    @ApiResponse(responseCode = "200", description = "Vínculo encontrado")
    @GetMapping("/{produtoId}/{ingredienteId}")
    public ProdutoIngredienteDto buscarPorId(
            @PathVariable Long produtoId,
            @PathVariable Long ingredienteId) {

        return service.buscarPorId(produtoId, ingredienteId);
    }

    @Operation(summary = "Remover ingrediente de um produto", description = "Remove um ingrediente da ficha técnica do produto.")
    @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso")
    @DeleteMapping("/{produtoId}/{ingredienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long produtoId,
            @PathVariable Long ingredienteId) {

        service.excluir(produtoId, ingredienteId);
    }
}
