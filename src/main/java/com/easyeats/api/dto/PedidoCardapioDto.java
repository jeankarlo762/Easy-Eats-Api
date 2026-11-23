package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.util.List;

import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.TipoVendaEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Pedido feito pelo cliente no cardápio digital")
public record PedidoCardapioDto(
        @NotBlank(message = "Informe seu nome")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        String nome,

        @NotBlank(message = "Informe seu telefone")
        @Size(max = 20, message = "Telefone inválido")
        @Schema(example = "(11) 99999-8888")
        String telefone,

        @Schema(description = "DELIVERY (padrão, exige endereço) ou RETIRADA", example = "DELIVERY")
        TipoVendaEnum tipoEntrega,

        @Valid
        Endereco endereco,

        @NotNull(message = "Escolha a forma de pagamento")
        FormaPagamentoEnum formaPagamento,

        @Positive(message = "O troco deve ser para um valor maior que zero")
        @Schema(description = "Só em dinheiro: valor que o cliente vai entregar", example = "100.00")
        BigDecimal trocoPara,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao,

        @NotEmpty(message = "Adicione ao menos um item")
        @Size(max = 50, message = "Pedido com itens demais")
        @Valid
        List<Item> itens
) {
    public record Item(
            @NotNull(message = "O produto é obrigatório") Long produtoId,
            @NotNull(message = "A quantidade é obrigatória")
            @Positive(message = "A quantidade deve ser maior que zero")
            @Max(value = 50, message = "Quantidade máxima por item: 50")
            Integer quantidade
    ) {
    }

    public record Endereco(
            @Size(max = 8, message = "CEP inválido") String cep,
            @Size(max = 150, message = "Rua muito longa") String rua,
            @Size(max = 20, message = "Número muito longo") String numero,
            @Size(max = 80, message = "Complemento muito longo") String complemento,
            @Size(max = 80, message = "Bairro muito longo") String bairro,
            @Size(max = 80, message = "Cidade muito longa") String cidade,
            @Size(max = 120, message = "Referência muito longa") String referencia
    ) {
    }
}
