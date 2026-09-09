package com.easy.eats.entrega.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.entrega.dto.RelatorioDelivery;
import com.easy.eats.entrega.dto.RelatorioDelivery.EntregasDia;
import com.easy.eats.entrega.dto.RelatorioDelivery.RankingEntregador;
import com.easy.eats.entrega.enums.StatusEntrega;
import com.easy.eats.entrega.model.Entrega;
import com.easy.eats.entrega.repository.EntregaRepository;
import com.easy.eats.entregador.model.Entregador;
import com.easy.eats.entregador.repository.EntregadorRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class EntregaService {

    @Autowired
    private EntregaRepository repository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Entrega> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaIdOrderByDtCriacaoDesc(SecurityUtils.getEmpresaId());
    }

    public Optional<Entrega> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public Entrega criar(Entrega entrega) {
        Integer empresaId = SecurityUtils.getEmpresaId();

        entrega.setId(null);
        entrega.setEmpresa(empresaRepository.getReferenceById(empresaId));
        entrega.setEntregador(entregadorDaMesmaEmpresa(entrega.getEntregador(), empresaId));
        entrega.setStatus(StatusEntrega.AGUARDANDO_RETIRADA);
        entrega.setDtCriacao(LocalDateTime.now());

        return repository.save(entrega);
    }

    /** Avança a entrega: Aguardando Retirada → A Caminho → Entregue. */
    public Entrega avancarStatus(Integer id) {
        Entrega entrega = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega não encontrada"));

        switch (entrega.getStatus()) {
            case AGUARDANDO_RETIRADA -> {
                entrega.setStatus(StatusEntrega.A_CAMINHO);
                entrega.setDtSaida(LocalDateTime.now());
            }
            case A_CAMINHO -> {
                entrega.setStatus(StatusEntrega.ENTREGUE);
                entrega.setDtEntrega(LocalDateTime.now());
            }
            case ENTREGUE -> {
                // já está no estágio final — nada a fazer
            }
        }

        return repository.save(entrega);
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }

    public RelatorioDelivery relatorio() {
        List<Entrega> entregas = listarTodos();

        List<Entrega> entregues = entregas.stream()
                .filter(e -> e.getStatus() == StatusEntrega.ENTREGUE && e.getDtEntrega() != null)
                .toList();

        double tempoMedio = entregues.isEmpty()
                ? 0
                : entregues.stream()
                        .mapToLong(e -> java.time.Duration.between(e.getDtCriacao(), e.getDtEntrega()).toMinutes())
                        .average()
                        .orElse(0);

        long pendentes = entregas.stream()
                .filter(e -> e.getStatus() != StatusEntrega.ENTREGUE)
                .count();

        Map<DayOfWeek, Long> porDia = entregues.stream()
                .collect(Collectors.groupingBy(e -> e.getDtEntrega().getDayOfWeek(), Collectors.counting()));

        List<EntregasDia> porDiaSemana = new ArrayList<>();
        for (DayOfWeek dia : DayOfWeek.values()) {
            String label = dia.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
            porDiaSemana.add(new EntregasDia(capitalizar(label), porDia.getOrDefault(dia, 0L)));
        }

        Map<String, Long> porEntregador = entregues.stream()
                .filter(e -> e.getEntregador() != null)
                .collect(Collectors.groupingBy(e -> e.getEntregador().getNome(), Collectors.counting()));

        List<RankingEntregador> ranking = porEntregador.entrySet().stream()
                .map(e -> new RankingEntregador(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RankingEntregador::entregas).reversed())
                .toList();

        return new RelatorioDelivery(entregues.size(), tempoMedio, pendentes, porDiaSemana, ranking);
    }

    private String capitalizar(String texto) {
        if (texto.isEmpty()) return texto;
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private Entregador entregadorDaMesmaEmpresa(Entregador entregadorRecebido, Integer empresaId) {
        if (entregadorRecebido == null || entregadorRecebido.getId() == null) {
            throw new IllegalArgumentException("O entregador é obrigatório");
        }
        return entregadorRepository.findByIdAndEmpresaId(entregadorRecebido.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Entregador informado não existe ou não pertence à sua empresa"));
    }
}
