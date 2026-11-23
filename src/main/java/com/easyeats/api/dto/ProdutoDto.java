
package com.easyeats.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para cadastro e atualização de produtos")
public record ProdutoDto(

        @Schema(
                description = "Identificador único do produto",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Nome do produto",
                example = "Hambúrguer Artesanal",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Schema(
                description = "Descrição detalhada do produto",
                example = "Hambúrguer artesanal com queijo e salada"
        )
        String descricao,

        @Schema(
                description = "Indica se o produto está ativo",
                example = "S"
        )
        String flativo,

        @Schema(
                description = "Identificador da categoria vinculada ao produto",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @Schema(
                description = "Preço de venda do produto",
                example = "29.90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal preco

) {
}
