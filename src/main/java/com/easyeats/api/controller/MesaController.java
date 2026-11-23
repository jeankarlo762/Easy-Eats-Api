package com.easyeats.api.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.easyeats.api.dto.MesaDto;
import com.easyeats.api.service.MesaService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mesa")
@RequiredArgsConstructor
@Tag(name = "Mesas", description = "Cadastro das mesas do restaurante. A ocupação das mesas é consultada em Vendas.")
@SecurityRequirement(name = "bearerAuth")
public class MesaController {

    private final MesaService service;

    @Operation(summary = "Cadastrar mesa", description = "Cadastra uma nova mesa. Não aceita dois números iguais.")
    @ApiResponse(responseCode = "201", description = "Mesa cadastrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (o número deve ser maior que zero)")
    @ApiResponse(responseCode = "422", description = "Já existe uma mesa com esse número")
    @PostMapping
    public ResponseEntity<MesaDto> criar(@Valid @RequestBody MesaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @Operation(summary = "Listar mesas", description = "Retorna as mesas ativas.")
    @ApiResponse(responseCode = "200", description = "Lista de mesas retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<MesaDto>> listar() {
        List<MesaDto> mesas = service.listarAtivas();

        if (mesas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mesas);
    }

    @Operation(summary = "Buscar mesa por ID", description = "Retorna os dados de uma mesa específica.")
    @ApiResponse(responseCode = "200", description = "Mesa encontrada")
    @ApiResponse(responseCode = "404", description = "Mesa não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<MesaDto> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar mesa", description = "Altera os dados de uma mesa existente.")
    @ApiResponse(responseCode = "200", description = "Mesa alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Mesa não encontrada")
    @ApiResponse(responseCode = "422", description = "Já existe uma mesa com esse número")
    @PutMapping("/{id}")
    public ResponseEntity<MesaDto> alterar(
            @PathVariable Long id,
            @Valid @RequestBody MesaDto dto) {

        MesaDto atualizado = service.alterar(id, dto);

        if (atualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(atualizado);
    }

    @Operation(summary = "Excluir mesa", description = "Exclusão lógica: a mesa passa a inativa. Não é possível excluir uma mesa que tenha venda aberta.")
    @ApiResponse(responseCode = "204", description = "Mesa excluída com sucesso")
    @ApiResponse(responseCode = "422", description = "A mesa tem uma venda aberta e não pode ser excluída")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
