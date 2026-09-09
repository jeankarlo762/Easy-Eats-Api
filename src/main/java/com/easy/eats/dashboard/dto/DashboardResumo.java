package com.easy.eats.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

import com.easy.eats.venda.quicksort.ProdutoRanking;

public record DashboardResumo(
        BigDecimal vendasHoje,
        Double variacaoVendas,
        long pedidosHoje,
        Double variacaoPedidos,
        BigDecimal ticketMedioHoje,
        Double variacaoTicketMedio,
        BigDecimal lucroHoje,
        Double variacaoLucro,
        long emPreparo,
        long prontos,
        long entreguesHoje,
        long estoqueCritico,
        List<PontoVendaDia> vendasSemana,
        List<FormaPagamentoResumo> formasPagamento,
        List<ProdutoRanking> produtosMaisVendidos,
        List<PontoHorario> horarioMovimento) {

    public record PontoVendaDia(String dia, BigDecimal valor) {
    }

    public record FormaPagamentoResumo(String metodo, BigDecimal valor) {
    }

    public record PontoHorario(String hora, long pedidos) {
    }
}
