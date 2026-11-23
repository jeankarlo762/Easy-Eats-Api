package com.easyeats.api.dto.mapper;

import com.easyeats.api.dto.PagamentoVendaDto;
import com.easyeats.api.dto.VendaDto;
import com.easyeats.api.dto.VendaResumoDto;
import com.easyeats.api.entity.PagamentoVenda;
import com.easyeats.api.entity.Pedido;
import com.easyeats.api.entity.Venda;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VendaMapper {

    private final PedidoMapper pedidoMapper;

    public VendaDto toDto(Venda venda, BigDecimal valorTotal, List<Pedido> comandas,
            List<PagamentoVenda> pagamentos) {
        BigDecimal valorPago = pagamentos.stream()
                .map(PagamentoVenda::getValor)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new VendaDto(
                venda.getId(),
                venda.getTipo(),
                venda.getMesa() != null ? venda.getMesa().getId() : null,
                venda.getMesa() != null ? venda.getMesa().getNumero() : null,
                venda.getStatus(),
                venda.getNomeCliente(),
                venda.getObservacao(),
                venda.getUsuarioAbertura(),
                venda.getDataAbertura(),
                venda.getUsuarioFechamento(),
                venda.getDataFechamento(),
                valorTotal,
                valorPago,
                valorTotal.subtract(valorPago),
                venda.getCaixa() != null ? venda.getCaixa().getId() : null,
                venda.getMotivoCancelamento(),
                comandas.stream().map(pedidoMapper::toDto).toList(),
                pagamentos.stream().map(this::toDto).toList()
        );
    }

    public VendaResumoDto toResumo(Venda venda, BigDecimal valorTotal) {
        return new VendaResumoDto(
                venda.getId(),
                venda.getTipo(),
                venda.getMesa() != null ? venda.getMesa().getId() : null,
                venda.getMesa() != null ? venda.getMesa().getNumero() : null,
                venda.getStatus(),
                venda.getNomeCliente(),
                venda.getUsuarioAbertura(),
                venda.getDataAbertura(),
                venda.getUsuarioFechamento(),
                venda.getDataFechamento(),
                valorTotal
        );
    }

    public PagamentoVendaDto toDto(PagamentoVenda pagamento) {
        BigDecimal troco = pagamento.getValorRecebido() == null
                ? null
                : pagamento.getValorRecebido().subtract(pagamento.getValor());
        return new PagamentoVendaDto(
                pagamento.getId(),
                pagamento.getForma(),
                pagamento.getValor(),
                pagamento.getValorRecebido(),
                troco,
                pagamento.getUsuario(),
                pagamento.getDataPagamento()
        );
    }
}