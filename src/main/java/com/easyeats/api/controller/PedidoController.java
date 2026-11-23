package com.easyeats.api.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.easyeats.api.entity.StatusPedidoEnum;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.easyeats.api.dto.ItemPedidoDto;
import com.easyeats.api.dto.PedidoDto;
import com.easyeats.api.security.UsuarioAutenticado;
import com.easyeats.api.service.PedidoService;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/pedido")
@RequiredArgsConstructor
@Tag(name = "Pedidos (comandas)", description = "Comandas lançadas em uma venda. Cada comanda tem itens e passa pelos status ABERTO, EM_PREPARO, FINALIZADO (pronta) e CANCELADO. O estoque é baixado quando a comanda entra em preparo.")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService service;

    @Operation(summary = "Lançar comanda", description = "Cria uma comanda com seus itens e a vincula a uma venda. Em MESA (padrão), informe mesaId: a venda da mesa é aberta na hora ou reaproveitada se já estiver aberta. Em RETIRADA, informe o nome do cliente, sem mesa. DELIVERY ainda não está disponível. O valor dos itens vem do preço ativo do produto, e o estoque é apenas validado, não baixado.")
    @ApiResponse(responseCode = "201", description = "Comanda lançada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, produto inativo ou sem preço ativo")
    @ApiResponse(responseCode = "422", description = "Regra de negócio não atendida (estoque insuficiente, mesa não informada, nome do cliente da retirada, delivery indisponível)")
    @PostMapping
    public ResponseEntity<PedidoDto> criar(@Valid @RequestBody PedidoDto pedido,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.salvar(pedido, UsuarioAutenticado.de(authentication)));
    }

    @Operation(summary = "Listar comandas", description = "Retorna todas as comandas, de qualquer status.")
    @ApiResponse(responseCode = "200", description = "Lista de comandas retornada com sucesso")
    @ApiResponse(responseCode = "204", description = "Não há comandas cadastradas")
    @GetMapping
    public ResponseEntity<List<PedidoDto>> listarTodos() {
        List<PedidoDto> pedidos = service.listarTodos();
        if (pedidos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Buscar comanda por ID", description = "Retorna uma comanda com seus itens.")
    @ApiResponse(responseCode = "200", description = "Comanda encontrada")
    @ApiResponse(responseCode = "404", description = "Comanda não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDto> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar comanda", description = "Substitui os itens, o cliente e a observação da comanda e, se informado, o status. Só é permitido enquanto a venda estiver aberta, e a comanda não pode mudar de mesa.")
    @ApiResponse(responseCode = "200", description = "Comanda alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou produto inativo")
    @ApiResponse(responseCode = "404", description = "Comanda não encontrada")
    @ApiResponse(responseCode = "422", description = "Venda já finalizada ou cancelada, troca de mesa ou estoque insuficiente")
    @PutMapping("/{id}")
    public ResponseEntity<PedidoDto> alterar(
            @PathVariable Long id,
            @Valid @RequestBody PedidoDto pedido) {

        PedidoDto atualizado = service.alterar(id, pedido);
        if (atualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(atualizado);
    }

    @Operation(summary = "Atualizar status da comanda", description = "Muda o status para ABERTO, EM_PREPARO, FINALIZADO (pronta) ou CANCELADO. Ao entrar em EM_PREPARO, baixa os ingredientes do estoque uma única vez. Ao passar de EM_PREPARO para FINALIZADO, envia notificação push ao aplicativo.")
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso")
    @ApiResponse(responseCode = "422", description = "Status inválido, estoque insuficiente ou venda já finalizada ou cancelada")
    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<PedidoDto> atualizarStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status: ABERTO, EM_PREPARO, FINALIZADO ou CANCELADO", example = "EM_PREPARO") @PathVariable String status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @Operation(summary = "Excluir comanda", description = "Exclui a comanda. Só é permitido enquanto a venda estiver aberta.")
    @ApiResponse(responseCode = "204", description = "Comanda excluída com sucesso")
    @ApiResponse(responseCode = "422", description = "A venda da comanda já foi finalizada ou cancelada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar comandas por status", description = "Retorna as comandas com o status informado. Para FINALIZADO e CANCELADO, devolve no máximo 10 comandas do dia, da mais recente para trás; use antesDe para continuar a consulta.")
    @ApiResponse(responseCode = "200", description = "Lista de comandas retornada com sucesso")
    @GetMapping(params = "status")
    public ResponseEntity<List<PedidoDto>> listarPorStatus(
            @Parameter(description = "Status das comandas: ABERTO, EM_PREPARO, FINALIZADO ou CANCELADO", example = "EM_PREPARO") @RequestParam StatusPedidoEnum status,
            @Parameter(description = "Traz só comandas criadas antes desta data e hora (ISO). Usado para continuar a consulta de FINALIZADO e CANCELADO", example = "2026-03-10T18:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime antesDe) {
        return ResponseEntity.ok(service.listarPorStatus(status, antesDe));
    }

    @Operation(summary = "Validar estoque de uma lista de itens", description = "Confere se há ingredientes suficientes para os itens informados, sem lançar comanda e sem baixar estoque.")
    @ApiResponse(responseCode = "204", description = "Há estoque suficiente para todos os itens")
    @ApiResponse(responseCode = "400", description = "Produto inativo ou sem preço ativo")
    @ApiResponse(responseCode = "422", description = "Estoque insuficiente")
    @PostMapping("/validar-estoque")
    public ResponseEntity<Void> validarEstoque(@RequestBody List<ItemPedidoDto> itens) {
        service.validarEstoque(itens);
        return ResponseEntity.noContent().build();
    }
}
