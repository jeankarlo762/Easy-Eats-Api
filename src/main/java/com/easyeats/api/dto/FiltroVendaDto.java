package com.easyeats.api.dto;

import com.easyeats.api.entity.StatusVendaEnum;

import java.time.LocalDate;

/** Filtros opcionais da listagem de vendas. Campos nulos não filtram. */
public record FiltroVendaDto(
        StatusVendaEnum status,
        Long mesaId,
        String usuario,             // quem abriu ou encerrou
        LocalDate inicio,           // data de referência, inclusivo
        LocalDate fim               // data de referência, inclusivo
) {
    // Data de referência: fechamento (finalizada ou cancelada) ou, se a venda ainda está aberta, abertura.
}