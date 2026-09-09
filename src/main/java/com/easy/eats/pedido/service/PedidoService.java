package com.easy.eats.pedido.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.pedido.bst.ArvorePedido;
import com.easy.eats.pedido.dto.RelatorioCozinha;
import com.easy.eats.pedido.dto.RelatorioCozinha.ItemPreparado;
import com.easy.eats.pedido.dto.RelatorioCozinha.TempoHorario;
import com.easy.eats.pedido.enums.StatusPedido;
import com.easy.eats.pedido.model.Pedido;
import com.easy.eats.pedido.repository.PedidoRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public Pedido criar(Pedido pedido) {
        pedido.setId(null);
        pedido.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        pedido.setStatus(StatusPedido.AGUARDANDO);
        pedido.setDataCriacao(LocalDateTime.now());
        return repository.save(pedido);
    }

    public Pedido salvar(Pedido pedido) {
        return repository.save(pedido);
    }

    public List<Pedido> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public Optional<Pedido> listarArvorePedido(Integer id) {
        List<Pedido> todosPedidos = listarTodos();

        if (todosPedidos.isEmpty()) {
            return Optional.empty();
        }

        ArvorePedido arvore = new ArvorePedido();

        for (Pedido p : todosPedidos) {
            arvore.inserir(p);
        }

        Pedido encontrado = arvore.buscar(id);

        return Optional.ofNullable(encontrado);
    }

    public Optional<Pedido> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }

    public List<Pedido> obterFilaDePedidos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll().stream()
                    .filter(p -> p.getStatus() == StatusPedido.AGUARDANDO || p.getStatus() == StatusPedido.PREPARANDO)
                    .toList();
        }
        return repository.findFilaAtivaPorEmpresa(SecurityUtils.getEmpresaId());
    }

    public Pedido iniciarPreparo(Integer id) {
        Pedido pedido = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == StatusPedido.AGUARDANDO) {
            pedido.setStatus(StatusPedido.PREPARANDO);
            pedido.setDtInicioPreparo(LocalDateTime.now());
            return repository.save(pedido);
        }

        return pedido;
    }

    public Pedido marcarComoPronto(Integer id) {
        Pedido pedido = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(StatusPedido.PRONTO);
        pedido.setDtPronto(LocalDateTime.now());
        return repository.save(pedido);
    }

    /**
     * Pedidos ainda na fila (aguardando ou preparando) há mais tempo do que o
     * razoável — não há um limite configurável em lugar nenhum, então usa o
     * mesmo valor fixo assumido nas telas de cozinha atuais.
     */
    private static final long LIMITE_MINUTOS_ATRASO = 20;

    public RelatorioCozinha relatorioCozinha() {
        List<Pedido> pedidos = listarTodos();
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();

        List<Pedido> prontosHoje = pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.PRONTO)
                .filter(p -> p.getDtPronto() != null && hoje.equals(p.getDtPronto().toLocalDate()))
                .toList();

        List<Long> temposPreparoMinutos = prontosHoje.stream()
                .filter(p -> p.getDtInicioPreparo() != null)
                .map(p -> Duration.between(p.getDtInicioPreparo(), p.getDtPronto()).toMinutes())
                .toList();

        double tempoMedioPreparo = temposPreparoMinutos.isEmpty()
                ? 0
                : temposPreparoMinutos.stream().mapToLong(Long::longValue).average().orElse(0);

        long pedidosAtrasados = pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.AGUARDANDO || p.getStatus() == StatusPedido.PREPARANDO)
                .filter(p -> p.getDataCriacao() != null
                        && Duration.between(p.getDataCriacao(), agora).toMinutes() > LIMITE_MINUTOS_ATRASO)
                .count();

        Map<String, Long> itensPorNome = prontosHoje.stream()
                .collect(Collectors.groupingBy(Pedido::getNomeProduto, Collectors.counting()));

        List<ItemPreparado> itensMaisPreparados = itensPorNome.entrySet().stream()
                .map(e -> new ItemPreparado(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ItemPreparado::quantidade).reversed())
                .limit(5)
                .toList();

        Map<Integer, List<Long>> temposPorHora = new TreeMap<>();
        for (Pedido p : prontosHoje) {
            if (p.getDtInicioPreparo() == null) {
                continue;
            }
            int hora = p.getDtInicioPreparo().getHour();
            long minutos = Duration.between(p.getDtInicioPreparo(), p.getDtPronto()).toMinutes();
            temposPorHora.computeIfAbsent(hora, k -> new ArrayList<>()).add(minutos);
        }

        List<TempoHorario> tempoPorHorario = temposPorHora.entrySet().stream()
                .map(e -> new TempoHorario(e.getKey() + "h",
                        e.getValue().stream().mapToLong(Long::longValue).average().orElse(0)))
                .toList();

        return new RelatorioCozinha(prontosHoje.size(), tempoMedioPreparo, pedidosAtrasados, itensMaisPreparados,
                tempoPorHorario);
    }
}
