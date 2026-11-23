package com.easyeats.api.dto;

import java.math.BigDecimal;
import java.util.List;

import com.easyeats.api.entity.FormaPagamentoEnum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cardápio digital público: produtos ativos com preço, agrupados por categoria")
public record CardapioPublicoDto(
        String nomeEstabelecimento,
        boolean aceitaDelivery,
        boolean aceitaRetirada,
        List<FormaPagamentoEnum> formasPagamento,
        List<Categoria> categorias
) {
    public record Categoria(Long id, String nome, List<Produto> produtos) {
    }

    public record Produto(Long id, String nome, String descricao, BigDecimal preco) {
    }
}
