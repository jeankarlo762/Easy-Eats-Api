package com.easy.eats.comanda.model;

import java.util.List;

import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.usuario.model.Usuario;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Corpo de POST /comanda/{id}/itens: uma nova rodada de pedido (ficará uma
 * nova Venda) para uma comanda já aberta. Único ponto do módulo de comandas
 * onde um corpo de requisição dedicado é usado em vez de uma entidade
 * diretamente, porque combina dois recursos (usuário + lista de itens) que
 * não correspondem a nenhuma entidade única.
 */
@Getter
@Setter
public class ItensComandaRequest {

    @NotNull(message = "O usuário é obrigatório")
    private Usuario usuario;

    /**
     * Os itens NÃO são validados em cascata (sem {@code @Valid} no elemento) de
     * propósito: {@link ItemVenda} exige {@code venda} (@NotNull) e, nesta
     * requisição, a venda ainda não existe — ela é criada e associada a cada
     * item dentro de {@code ComandaService.adicionarItens}. Cascatear aqui
     * rejeitaria com 400 todo lançamento de itens em comanda.
     *
     * A validação de cada item acontece no ComandaService (quantidade e preço)
     * e no ItemVendaService (produto, adicionais e composição pertencentes ao
     * produto/empresa), além da validação da entidade no momento do persist.
     */
    @NotEmpty(message = "Informe ao menos um item")
    private List<ItemVenda> itens;
}
