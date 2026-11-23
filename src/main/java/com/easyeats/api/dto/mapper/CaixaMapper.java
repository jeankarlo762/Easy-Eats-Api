package com.easyeats.api.dto.mapper;

import com.easyeats.api.dto.CaixaDto;
import com.easyeats.api.dto.MovimentacaoCaixaDto;
import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.MovimentacaoCaixa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CaixaMapper {

    public CaixaDto toDto(Caixa caixa, BigDecimal saldoAtual) {
        if (caixa == null) {
            return null;
        }
        return new CaixaDto(
                caixa.getId(),
                caixa.getStatus(),
                caixa.getFundoCaixa(),
                caixa.getUsuarioAbertura(),
                caixa.getDataAbertura(),
                caixa.getObservacaoAbertura(),
                caixa.getUsuarioFechamento(),
                caixa.getDataFechamento(),
                caixa.getValorEsperado(),
                caixa.getValorContado(),
                caixa.getDiferenca(),
                caixa.getObservacaoFechamento(),
                saldoAtual
        );
    }

    public MovimentacaoCaixaDto toDto(MovimentacaoCaixa movimentacao) {
        if (movimentacao == null) {
            return null;
        }
        return new MovimentacaoCaixaDto(
                movimentacao.getId(),
                movimentacao.getCaixa() != null ? movimentacao.getCaixa().getId() : null,
                movimentacao.getVendaId(),
                movimentacao.getTipo(),
                movimentacao.getValor(),
                movimentacao.getDescricao(),
                movimentacao.getUsuario(),
                movimentacao.getDataMovimentacao()
        );
    }
}
