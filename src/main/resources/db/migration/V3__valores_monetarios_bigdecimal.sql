-- Todo valor monetário do sistema era double precision (IEEE 754 binário),
-- que não representa exatamente frações decimais (o clássico 0.1 + 0.2 !=
-- 0.3). O erro se acumula justamente nas operações que este sistema mais
-- faz: somar itens de uma comanda, fechar caixa comparando apurado com
-- informado, ranquear faturamento. As entidades Java passam a usar
-- BigDecimal; aqui o schema acompanha com NUMERIC, que guarda o valor
-- decimal exato. USING converte os dados existentes sem perdê-los.

ALTER TABLE tbadicional
    ALTER COLUMN preco TYPE numeric(12,2) USING preco::numeric(12,2);

ALTER TABLE tbcaixa
    ALTER COLUMN valor_inicial TYPE numeric(12,2) USING valor_inicial::numeric(12,2),
    ALTER COLUMN valor_apurado_informado TYPE numeric(12,2) USING valor_apurado_informado::numeric(12,2),
    ALTER COLUMN valor_apurado_sistema TYPE numeric(12,2) USING valor_apurado_sistema::numeric(12,2),
    ALTER COLUMN diferenca TYPE numeric(12,2) USING diferenca::numeric(12,2);

ALTER TABLE tbcashback_config
    ALTER COLUMN percentual_acumulo TYPE numeric(5,2) USING percentual_acumulo::numeric(5,2),
    ALTER COLUMN valor_minimo_para_acumular TYPE numeric(12,2) USING valor_minimo_para_acumular::numeric(12,2);

ALTER TABLE tbcliente
    ALTER COLUMN saldo_cashback TYPE numeric(12,2) USING saldo_cashback::numeric(12,2);

ALTER TABLE tbcomanda
    ALTER COLUMN valor_total TYPE numeric(12,2) USING valor_total::numeric(12,2);

ALTER TABLE tbcupom
    ALTER COLUMN valor_desconto TYPE numeric(12,2) USING valor_desconto::numeric(12,2),
    ALTER COLUMN valor_minimo_pedido TYPE numeric(12,2) USING valor_minimo_pedido::numeric(12,2);

ALTER TABLE tbitemcardapio
    ALTER COLUMN preco_override TYPE numeric(12,2) USING preco_override::numeric(12,2);

ALTER TABLE tbitemvenda
    ALTER COLUMN preco_unitario TYPE numeric(12,2) USING preco_unitario::numeric(12,2),
    ALTER COLUMN custo_unitario TYPE numeric(12,2) USING custo_unitario::numeric(12,2),
    ALTER COLUMN valor_total TYPE numeric(12,2) USING valor_total::numeric(12,2),
    ALTER COLUMN desconto TYPE numeric(12,2) USING desconto::numeric(12,2);

ALTER TABLE tbitemvendaadicional
    ALTER COLUMN preco TYPE numeric(12,2) USING preco::numeric(12,2);

ALTER TABLE tbmovimentacao_financeira
    ALTER COLUMN valor TYPE numeric(12,2) USING valor::numeric(12,2);

ALTER TABLE tbpagamento
    ALTER COLUMN valor TYPE numeric(12,2) USING valor::numeric(12,2);

ALTER TABLE tbproduto
    ALTER COLUMN preco TYPE numeric(12,2) USING preco::numeric(12,2),
    ALTER COLUMN custo TYPE numeric(12,2) USING custo::numeric(12,2);

ALTER TABLE tbvenda
    ALTER COLUMN valor_total TYPE numeric(12,2) USING valor_total::numeric(12,2),
    ALTER COLUMN desconto TYPE numeric(12,2) USING desconto::numeric(12,2);
