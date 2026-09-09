package com.easy.eats.pedido.dto;

import java.util.List;

public record RelatorioCozinha(
        long pedidosPreparadosHoje,
        double tempoMedioPreparoMinutos,
        long pedidosAtrasados,
        List<ItemPreparado> itensMaisPreparados,
        List<TempoHorario> tempoPorHorario) {

    public record ItemPreparado(String nome, long quantidade) {
    }

    public record TempoHorario(String hora, double minutos) {
    }
}
