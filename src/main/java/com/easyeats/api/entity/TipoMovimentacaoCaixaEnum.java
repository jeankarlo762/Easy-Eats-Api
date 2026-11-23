package com.easyeats.api.entity;

public enum TipoMovimentacaoCaixaEnum {
    FUNDO_INICIAL(true),
    SUPRIMENTO(true),
    SANGRIA(false),
    VENDA(true);

    private final boolean entrada;

    TipoMovimentacaoCaixaEnum(boolean entrada) {
        this.entrada = entrada;
    }

    /** true quando o valor soma no caixa; false quando retira dinheiro dele. */
    public boolean isEntrada() {
        return entrada;
    }
}
