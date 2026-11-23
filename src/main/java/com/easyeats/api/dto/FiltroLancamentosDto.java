package com.easyeats.api.dto;

import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;

import java.time.LocalDate;

/** Filtros opcionais dos lançamentos do caixa. Campos nulos não filtram. */
public record FiltroLancamentosDto(
        LocalDate inicio,           // inclusivo
        LocalDate fim,              // inclusivo
        TipoMovimentacaoCaixaEnum tipo,
        String usuario,
        Long caixaId
) {
}
