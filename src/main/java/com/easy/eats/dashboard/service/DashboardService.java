package com.easy.eats.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.dashboard.dto.DashboardResumo;
import com.easy.eats.dashboard.dto.DashboardResumo.FormaPagamentoResumo;
import com.easy.eats.dashboard.dto.DashboardResumo.PontoHorario;
import com.easy.eats.dashboard.dto.DashboardResumo.PontoVendaDia;
import com.easy.eats.estoque.service.EstoqueService;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.pagamento.repository.PagamentoRepository;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.quicksort.ProdutoRanking;
import com.easy.eats.venda.repository.VendaRepository;
import com.easy.eats.venda.service.VendaService;

/**
 * Agrega dados já existentes (Venda, Pagamento, Estoque) num único resumo
 * para o Dashboard. Não introduz nenhuma tabela nova — é só leitura.
 */
@Service
public class DashboardService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private VendaService vendaService;

    public DashboardResumo resumo() {
        Integer empresaId = SecurityUtils.getEmpresaId();
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);

        List<Venda> vendasEmpresa = vendaRepository.findAllByEmpresaId(empresaId);

        List<Venda> vendasHoje = filtrarPorDia(vendasEmpresa, hoje);
        List<Venda> vendasOntem = filtrarPorDia(vendasEmpresa, ontem);

        BigDecimal totalVendasHoje = somaValorTotal(vendasHoje);
        BigDecimal totalVendasOntem = somaValorTotal(vendasOntem);

        BigDecimal ticketMedioHoje = ticketMedio(totalVendasHoje, vendasHoje.size());
        BigDecimal ticketMedioOntem = ticketMedio(totalVendasOntem, vendasOntem.size());

        BigDecimal lucroHoje = lucro(vendasHoje);
        BigDecimal lucroOntem = lucro(vendasOntem);

        long emPreparo = contarPorStatus(vendasHoje, "Preparando");
        long prontos = contarPorStatus(vendasHoje, "Pronto");
        long entreguesHoje = contarPorStatus(vendasHoje, "Entregue");

        return new DashboardResumo(
                totalVendasHoje,
                variacao(totalVendasHoje, totalVendasOntem),
                vendasHoje.size(),
                variacao(BigDecimal.valueOf(vendasHoje.size()), BigDecimal.valueOf(vendasOntem.size())),
                ticketMedioHoje,
                variacao(ticketMedioHoje, ticketMedioOntem),
                lucroHoje,
                variacao(lucroHoje, lucroOntem),
                emPreparo,
                prontos,
                entreguesHoje,
                estoqueService.listarAbaixoDoMinimo().size(),
                vendasSemana(vendasEmpresa, hoje),
                formasPagamento(empresaId, hoje),
                produtosMaisVendidos(),
                horarioMovimento(vendasHoje));
    }

    private List<Venda> filtrarPorDia(List<Venda> vendas, LocalDate dia) {
        return vendas.stream()
                .filter(v -> dia.equals(dataCriacao(v)))
                .collect(Collectors.toList());
    }

    private LocalDate dataCriacao(Venda venda) {
        if (venda.getDt_criacao() == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(venda.getDt_criacao()).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal somaValorTotal(List<Venda> vendas) {
        return vendas.stream()
                .map(Venda::getValor_total)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal ticketMedio(BigDecimal total, int quantidade) {
        if (quantidade == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal lucro(List<Venda> vendas) {
        BigDecimal lucro = BigDecimal.ZERO;
        for (Venda venda : vendas) {
            if (venda.getItens() == null) {
                continue;
            }
            for (ItemVenda item : venda.getItens()) {
                BigDecimal valorTotal = item.getValor_total() != null ? item.getValor_total() : BigDecimal.ZERO;
                BigDecimal custo = item.getCusto_unitario() != null && item.getQuantidade() != null
                        ? item.getCusto_unitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
                        : BigDecimal.ZERO;
                lucro = lucro.add(valorTotal).subtract(custo);
            }
        }
        return lucro;
    }

    private long contarPorStatus(List<Venda> vendas, String status) {
        return vendas.stream().filter(v -> status.equalsIgnoreCase(v.getStatus())).count();
    }

    /**
     * Percentual de variação de hoje sobre ontem. Nulo quando ontem foi zero
     * (não há base de comparação — evitaria uma divisão por zero ou um
     * "+infinito%" sem sentido para quem está lendo o card).
     */
    private Double variacao(BigDecimal hoje, BigDecimal ontem) {
        if (ontem == null || ontem.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return hoje.subtract(ontem)
                .divide(ontem, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<PontoVendaDia> vendasSemana(List<Venda> vendasEmpresa, LocalDate hoje) {
        List<PontoVendaDia> pontos = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            BigDecimal total = somaValorTotal(filtrarPorDia(vendasEmpresa, dia));
            String label = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"));
            pontos.add(new PontoVendaDia(label, total));
        }
        return pontos;
    }

    private List<FormaPagamentoResumo> formasPagamento(Integer empresaId, LocalDate hoje) {
        List<Pagamento> pagamentos = pagamentoRepository.findAllByVenda_Empresa_Id(empresaId);

        Map<String, BigDecimal> porMetodo = new TreeMap<>();
        for (Pagamento pagamento : pagamentos) {
            if (!hoje.equals(dataPagamento(pagamento))) {
                continue;
            }
            String metodo = pagamento.getMetodo() != null ? pagamento.getMetodo() : "Outro";
            BigDecimal valor = pagamento.getValor() != null ? pagamento.getValor() : BigDecimal.ZERO;
            porMetodo.merge(metodo, valor, BigDecimal::add);
        }

        return porMetodo.entrySet().stream()
                .map(e -> new FormaPagamentoResumo(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(FormaPagamentoResumo::valor).reversed())
                .collect(Collectors.toList());
    }

    private LocalDate dataPagamento(Pagamento pagamento) {
        if (pagamento.getDt_pagamento() == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(pagamento.getDt_pagamento()).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private List<ProdutoRanking> produtosMaisVendidos() {
        return vendaService.rankingProdutos().stream().limit(5).collect(Collectors.toList());
    }

    private List<PontoHorario> horarioMovimento(List<Venda> vendasHoje) {
        Map<Integer, Long> porHora = new TreeMap<>();
        for (Venda venda : vendasHoje) {
            if (venda.getDt_criacao() == null) {
                continue;
            }
            try {
                int hora = LocalDateTime.parse(venda.getDt_criacao()).getHour();
                porHora.merge(hora, 1L, Long::sum);
            } catch (Exception e) {
                // dt_criacao inválido — ignora este pedido na distribuição por horário
            }
        }
        return porHora.entrySet().stream()
                .map(e -> new PontoHorario(e.getKey() + "h", e.getValue()))
                .collect(Collectors.toList());
    }
}
