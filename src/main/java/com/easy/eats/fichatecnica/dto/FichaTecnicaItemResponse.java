package com.easy.eats.fichatecnica.dto;

import java.math.BigDecimal;

import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.produto.enums.UnidadeMedida;
import com.easy.eats.produto.model.Produto;

/**
 * Item da ficha técnica como a API devolve: os ids do vínculo
 * (produtoId, ingredienteId, quantidadeUtilizada) mais os dados do
 * ingrediente que a tela precisa para exibir e calcular custo.
 */
public record FichaTecnicaItemResponse(
        Integer id,
        Integer produtoId,
        Integer ingredienteId,
        String ingredienteNome,
        UnidadeMedida unidadeMedida,
        BigDecimal custoUnitario,
        BigDecimal quantidadeUtilizada,
        BigDecimal custoTotal) {

    public static FichaTecnicaItemResponse de(FichaTecnicaItem item) {
        Produto ingrediente = item.getIngrediente();
        BigDecimal custoUnitario = ingrediente.getCusto() != null ? ingrediente.getCusto() : BigDecimal.ZERO;
        return new FichaTecnicaItemResponse(
                item.getId(),
                item.getProduto().getId(),
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getUnidadeMedida(),
                custoUnitario,
                item.getQuantidadeUtilizada(),
                custoUnitario.multiply(item.getQuantidadeUtilizada()));
    }
}
