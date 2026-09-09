-- Diferente de outros bancos, o Postgres NÃO cria índice automático em
-- coluna de chave estrangeira. empresa_id é o filtro mais usado em todo o
-- sistema (é assim que o multi-tenant funciona: praticamente toda consulta
-- de listagem filtra por ele). Sem índice, cada uma dessas consultas faz
-- table scan completo — invisível hoje com poucos dados de teste, mas é
-- exatamente o tipo de problema que só aparece com volume real em produção.

CREATE INDEX idx_caixa_empresa_id ON tbcaixa (empresa_id);
CREATE INDEX idx_cardapio_empresa_id ON tbcardapio (empresa_id);
CREATE INDEX idx_cashback_config_empresa_id ON tbcashback_config (empresa_id);
CREATE INDEX idx_categoria_empresa_id ON tbcategoria (empresa_id);
CREATE INDEX idx_cliente_empresa_id ON tbcliente (empresa_id);
CREATE INDEX idx_comanda_empresa_id ON tbcomanda (empresa_id);
CREATE INDEX idx_cupom_empresa_id ON tbcupom (empresa_id);
CREATE INDEX idx_fornecedor_empresa_id ON tbfornecedor (empresa_id);
CREATE INDEX idx_mesa_empresa_id ON tbmesa (empresa_id);
CREATE INDEX idx_movimentacao_financeira_empresa_id ON tbmovimentacao_financeira (empresa_id);
CREATE INDEX idx_pedido_empresa_id ON tbpedido (empresa_id);
CREATE INDEX idx_produto_empresa_id ON tbproduto (empresa_id);
CREATE INDEX idx_usuario_empresa_id ON tbusuario (empresa_id);
CREATE INDEX idx_venda_empresa_id ON tbvenda (empresa_id);
