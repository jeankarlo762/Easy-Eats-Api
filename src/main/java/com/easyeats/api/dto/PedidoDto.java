package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.easyeats.api.entity.TipoVendaEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comanda com os itens pedidos")
public record PedidoDto(
        @Schema(description = "Identificador único da comanda", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @Schema(description = "Data e hora em que a comanda foi lançada", example = "2026-03-10T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataCriacao,
        @Schema(description = "Data e hora da última alteração da comanda", example = "2026-03-10T12:05:00", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime dataAlteracao,
        @Schema(description = "Identificador da mesa. Obrigatório quando tipoVenda é MESA; não usar em RETIRADA", example = "4")
        Long mesaId,
        @Schema(description = "Venda existente que receberá a comanda. Opcional: só aceito para incluir a comanda em uma retirada já aberta")
        Long vendaId,               // na criação: opcional, para incluir a comanda numa retirada já aberta
        @Schema(description = "Tipo da venda: MESA (padrão), RETIRADA ou DELIVERY (ainda indisponível)", example = "MESA")
        TipoVendaEnum tipoVenda,    // MESA (padrão), RETIRADA ou DELIVERY (ainda indisponível)
        @Schema(description = "Nome do cliente. Obrigatório na RETIRADA e opcional na mesa", example = "Ana")
        String cliente,
        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        @Schema(description = "Observação da comanda, com até 255 caracteres", example = "Sem cebola")
        String observacao,
        @Schema(description = "Status da comanda: ABERTO, EM_PREPARO, FINALIZADO (pronta) ou CANCELADO", example = "ABERTO")
        String status,
        @Schema(description = "Valor total dos itens, calculado pela API", example = "59.80", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal valorTotal,

        @NotEmpty(message = "O pedido deve ter ao menos um item")
        @Valid
        @Schema(description = "Itens da comanda. Informe ao menos um")
        List<ItemPedidoDto> itens
) {
}
