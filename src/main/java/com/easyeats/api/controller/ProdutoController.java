package com.easyeats.api.controller;

import com.easyeats.api.dto.ProdutoDto;
import com.easyeats.api.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produto")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos do Easy Eats")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

        private final ProdutoService service;

        @Operation(summary = "Cadastrar produto", description = "Cadastra um novo produto no sistema")
        @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso")
        @ApiResponse(responseCode = "400", description = "Dados do produto inválidos")
        @PostMapping
        public ResponseEntity<ProdutoDto> criar(
                        @Valid @RequestBody ProdutoDto produto) {

                ProdutoDto salvo = service.salvar(produto);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(salvo);
        }

        @Operation(summary = "Listar produtos", description = "Retorna todos os produtos cadastrados")
        @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
        @GetMapping
        public ResponseEntity<List<ProdutoDto>> listarTodos() {

                List<ProdutoDto> produtos = service.listarTodos();

                return ResponseEntity.ok(produtos);
        }

        @Operation(summary = "Buscar produto por ID", description = "Retorna os dados de um produto específico")
        @ApiResponse(responseCode = "200", description = "Produto encontrado")
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        @GetMapping("/{id}")
        public ResponseEntity<ProdutoDto> buscarPorId(
                        @PathVariable Long id) {

                return service.buscarPorId(id)

                                .map(ResponseEntity::ok)

                                .orElse(
                                                ResponseEntity.notFound()
                                                                .build());
        }

        @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
        @ApiResponse(responseCode = "400", description = "Dados do produto inválidos")
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        @PutMapping("/{id}")
        public ResponseEntity<ProdutoDto> alterar(

                        @PathVariable Long id,

                        @Valid @RequestBody ProdutoDto produto) {

                ProdutoDto atualizado = service.alterar(
                                id,
                                produto);

                if (atualizado == null) {

                        return ResponseEntity
                                        .notFound()
                                        .build();
                }

                return ResponseEntity.ok(
                                atualizado);
        }

        @Operation(summary = "Excluir produto", description = "Exclui um produto através do seu ID")
        @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> excluir(
                        @PathVariable Long id) {

                service.excluir(id);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}