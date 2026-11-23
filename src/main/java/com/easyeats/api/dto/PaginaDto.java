package com.easyeats.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Página de resultados")
public record PaginaDto<T>(
        @Schema(description = "Itens da página", accessMode = Schema.AccessMode.READ_ONLY)
        List<T> itens,
        @Schema(description = "Total de itens em todas as páginas", example = "45", accessMode = Schema.AccessMode.READ_ONLY)
        long total,
        @Schema(description = "Número da página atual, começando em 0", example = "0", accessMode = Schema.AccessMode.READ_ONLY)
        int pagina,
        @Schema(description = "Quantidade de itens por página", example = "20", accessMode = Schema.AccessMode.READ_ONLY)
        int tamanho,
        @Schema(description = "Total de páginas", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
        int totalPaginas
) {
    public static <T> PaginaDto<T> de(Page<T> page) {
        return new PaginaDto<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}
