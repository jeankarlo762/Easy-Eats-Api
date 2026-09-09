package com.easy.eats.entrega.dto;

import java.util.List;

public record RelatorioDelivery(
        long totalEntregas,
        double tempoMedioEntregaMinutos,
        long entregasPendentes,
        List<EntregasDia> porDiaSemana,
        List<RankingEntregador> ranking) {

    public record EntregasDia(String dia, long entregas) {
    }

    public record RankingEntregador(String nome, long entregas) {
    }
}
