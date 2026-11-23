package com.easyeats.api.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastro e alteração de categorias")
public record CategoriaDto(
        @Schema(description = "Identificador único da categoria", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        @NotBlank(message = "O nome é obrigatório")
        @Schema(description = "Nome da categoria", example = "Lanches")
        String nome,
        @Schema(description = "Indica se a categoria está ativa (S ou N)", example = "S")
        String flativo
) {
}
