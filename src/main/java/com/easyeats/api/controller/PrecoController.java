package com.easyeats.api.controller;

import com.easyeats.api.dto.PrecoDto;
import com.easyeats.api.service.PrecoService;

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
@RequestMapping("/preco")
@RequiredArgsConstructor
@Tag(name = "Preços", description = "Preço de venda dos produtos. Cada novo preço desativa o anterior, o que preserva o histórico.")
@SecurityRequirement(name = "bearerAuth")
public class PrecoController {

    private final PrecoService service;


    @Operation(summary = "Cadastrar preço", description = "Cadastra um novo preço para um produto ativo e desativa o preço anterior dele.")
    @ApiResponse(responseCode = "201", description = "Preço cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (o valor deve ser maior que zero)")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo")
    @PostMapping
    public ResponseEntity<PrecoDto> criar(
            @Valid
            @RequestBody
            PrecoDto preco
    ) {

        PrecoDto salvo =
                service.salvar(preco);

        if (salvo == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(salvo);
    }


    @Operation(summary = "Listar preços", description = "Retorna todos os preços cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de preços retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<PrecoDto>> listarTodos() {

        return ResponseEntity.ok(
                service.listarTodos()
        );
    }


    @Operation(summary = "Buscar preço por ID", description = "Retorna os dados de um preço específico.")
    @ApiResponse(responseCode = "200", description = "Preço encontrado")
    @ApiResponse(responseCode = "404", description = "Preço não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<PrecoDto> buscarPorId(
            @PathVariable Long id
    ) {

        return service.buscarPorId(id)

                .map(ResponseEntity::ok)

                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    @Operation(summary = "Listar preços de um produto", description = "Retorna os preços cadastrados para o produto informado.")
    @ApiResponse(responseCode = "200", description = "Lista de preços do produto retornada com sucesso")
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<PrecoDto>> listarPorProduto(
            @PathVariable Long produtoId
    ) {

        return ResponseEntity.ok(
                service.listarPorProduto(
                        produtoId
                )
        );
    }


    @Operation(summary = "Alterar preço", description = "Não sobrescreve o preço antigo: cria uma nova versão para o produto informado no corpo e desativa a anterior, preservando o histórico.")
    @ApiResponse(responseCode = "200", description = "Nova versão do preço criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo")
    @PutMapping("/{id}")
    public ResponseEntity<PrecoDto> alterar(
            @PathVariable Long id,

            @Valid
            @RequestBody
            PrecoDto preco
    ) {

        /*
         * Para alteração de preço,
         * é melhor criar uma nova versão
         * do preço em vez de sobrescrever
         * o histórico.
         */
        PrecoDto atualizado =
                service.salvar(
                        preco
                );

        if (atualizado == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                atualizado
        );
    }


    @Operation(summary = "Excluir preço", description = "Exclusão lógica: o preço passa a inativo.")
    @ApiResponse(responseCode = "204", description = "Preço excluído com sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id
    ) {

        service.excluir(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
