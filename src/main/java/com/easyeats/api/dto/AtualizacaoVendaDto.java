package com.easyeats.api.dto;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados alteráveis de uma venda aberta")
public record AtualizacaoVendaDto(
        @Size(max = 255, message = "O nome do cliente deve ter no máximo 255 caracteres")
        @Schema(description = "Nome do cliente, com até 255 caracteres", example = "Ana")
        String nomeCliente,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        @Schema(description = "Observação da venda, com até 255 caracteres", example = "Mesa perto da janela")
        String observacao
) {
}
