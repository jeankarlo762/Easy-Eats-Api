-- Venda não tinha nenhum vínculo com Cliente, só o texto livre nomeCliente —
-- sem isso não há histórico de compras por cliente, nem base para cupom
-- (limiteUsoPorCliente) ou cashback (saldoCashback) identificarem de quem
-- descontar ou creditar. A coluna é opcional: pedidos de balcão sem cadastro
-- continuam funcionando normalmente com só o nomeCliente.
-- Idempotente: bancos criados pelo Hibernate (ddl-auto=update, como o
-- banco atual do Railway) já têm tbvenda.cliente_id e a FK.
ALTER TABLE tbvenda
    ADD COLUMN IF NOT EXISTS cliente_id integer;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'tbvenda'::regclass
          AND c.contype = 'f'
          AND a.attname = 'cliente_id'
    ) THEN
        ALTER TABLE tbvenda
            ADD CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id) REFERENCES tbcliente (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_venda_cliente_id ON tbvenda (cliente_id);
