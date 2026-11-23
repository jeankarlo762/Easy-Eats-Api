package com.easyeats.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cancelar uma venda")
public record CancelamentoVendaDto(
        @NotBlank(message = "Informe o motivo do cancelamento")
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        @Schema(description = "Motivo do cancelamento, obrigatório, com até 255 caracteres", example = "Cliente desistiu do pedido")
        String motivo,

        // Devolve ao estoque os itens de comandas que baixaram o estoque mas ainda não foram usados
        // (ABERTO ou EM_PREPARO). Exige a senha do gerente (do usuário autenticado).
        @Schema(description = "Se verdadeiro, devolve ao estoque os ingredientes de comandas que já baixaram o estoque e ainda não foram usadas (ABERTO ou EM_PREPARO). Exige a senha do gerente", example = "false")
        Boolean devolverEstoque,

        @Schema(description = "Senha do usuário autenticado (Administrador ou Gerente). Obrigatória apenas quando devolverEstoque é verdadeiro")
        String senhaGerente
) {
}
