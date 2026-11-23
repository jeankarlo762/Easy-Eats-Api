package com.easyeats.api.dto;

import com.easyeats.api.entity.TipoVendaEnum;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para abrir uma venda")
public record AberturaVendaDto(
        // Opcional: sem tipo, vale MESA quando há mesaId e RETIRADA quando não há
        @Schema(description = "Tipo da venda: MESA, RETIRADA ou DELIVERY (ainda indisponível). Se omitido, vale MESA com mesaId e RETIRADA sem mesaId", example = "MESA")
        TipoVendaEnum tipo,

        @Schema(description = "Identificador da mesa. Obrigatório em MESA e não usado em RETIRADA", example = "4")
        Long mesaId,                // obrigatório para MESA; não se usa em RETIRADA

        @Size(max = 255, message = "O nome do cliente deve ter no máximo 255 caracteres")
        @Schema(description = "Nome do cliente. Obrigatório em RETIRADA", example = "Ana")
        String nomeCliente,         // obrigatório para RETIRADA

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        @Schema(description = "Observação da venda, com até 255 caracteres", example = "Cliente alérgico a amendoim")
        String observacao
) {
}
