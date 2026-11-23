package com.easyeats.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.easyeats.api.dto.ItemPedidoDto;
import com.easyeats.api.service.ItemPedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/item-pedido")
@RequiredArgsConstructor
@Tag(name = "Itens do pedido", description = "Itens de uma comanda. O valor unitário é fixado a partir do preço ativo do produto.")
@SecurityRequirement(name = "bearerAuth")
public class ItemPedidoController {

    private final ItemPedidoService service;

    @Operation(summary = "Adicionar item à comanda", description = "Inclui um item em uma comanda de venda aberta. O estoque é apenas validado, não baixado.")
    @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, produto inativo ou sem preço ativo")
    @ApiResponse(responseCode = "404", description = "Comanda não encontrada")
    @ApiResponse(responseCode = "422", description = "Estoque insuficiente ou venda já finalizada ou cancelada")
    @PostMapping("/pedido/{pedidoId}")
    public ResponseEntity<ItemPedidoDto> adicionar(
            @PathVariable Long pedidoId,
            @Valid @RequestBody ItemPedidoDto item) {

        ItemPedidoDto salvo = service.adicionar(pedidoId, item);
        if (salvo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @Operation(summary = "Listar itens de uma comanda", description = "Retorna os itens da comanda informada.")
    @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso")
    @ApiResponse(responseCode = "204", description = "A comanda não tem itens")
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ItemPedidoDto>> listarPorPedido(@PathVariable Long pedidoId) {
        List<ItemPedidoDto> itens = service.listarPorPedido(pedidoId);
        if (itens.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(itens);
    }

    @Operation(summary = "Buscar item por ID", description = "Retorna os dados de um item específico.")
    @ApiResponse(responseCode = "200", description = "Item encontrado")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ItemPedidoDto> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar item", description = "Altera a quantidade do item e, se o produto mudar, recalcula o valor unitário pelo preço ativo.")
    @ApiResponse(responseCode = "200", description = "Item alterado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, produto inativo ou sem preço ativo")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    @ApiResponse(responseCode = "422", description = "Venda já finalizada ou cancelada")
    @PutMapping("/{id}")
    public ResponseEntity<ItemPedidoDto> alterar(
            @PathVariable Long id,
            @Valid @RequestBody ItemPedidoDto item) {

        ItemPedidoDto atualizado = service.alterar(id, item);
        if (atualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(atualizado);
    }

    @Operation(summary = "Excluir item", description = "Remove o item da comanda. A comanda precisa continuar com ao menos um item.")
    @ApiResponse(responseCode = "204", description = "Item excluído com sucesso")
    @ApiResponse(responseCode = "409", description = "A comanda deve ter ao menos um item")
    @ApiResponse(responseCode = "422", description = "Venda já finalizada ou cancelada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
