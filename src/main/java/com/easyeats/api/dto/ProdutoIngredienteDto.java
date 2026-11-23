package com.easyeats.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vínculo entre um produto e um ingrediente")
public class ProdutoIngredienteDto {

    @Schema(description = "Identificador do produto", example = "1")
    private Long produtoId;

    @Schema(description = "Identificador do ingrediente", example = "1")
    private Long ingredienteId;

    @Schema(description = "Quantidade do ingrediente usada no produto", example = "0.15")
    private Double quantidade;
}
