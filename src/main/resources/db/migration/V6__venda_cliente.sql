-- Venda não tinha nenhum vínculo com Cliente, só o texto livre nomeCliente —
-- sem isso não há histórico de compras por cliente, nem base para cupom
-- (limiteUsoPorCliente) ou cashback (saldoCashback) identificarem de quem
-- descontar ou creditar. A coluna é opcional: pedidos de balcão sem cadastro
-- continuam funcionando normalmente com só o nomeCliente.
ALTER TABLE tbvenda
    ADD COLUMN cliente_id integer;

ALTER TABLE tbvenda
    ADD CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id) REFERENCES tbcliente (id);

CREATE INDEX idx_venda_cliente_id ON tbvenda (cliente_id);
