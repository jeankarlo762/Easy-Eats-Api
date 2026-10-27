-- Ingrediente = produto de natureza INSUMO com registro em tbestoque. Até
-- aqui ele não tinha unidade de medida, então "2" na ficha técnica podia ser
-- 2 unidades, 2 g ou 2 kg. A unidade vale tanto para o saldo em estoque
-- quanto para a quantidade utilizada na ficha técnica.
ALTER TABLE tbproduto ADD COLUMN unidade_medida character varying(10);
ALTER TABLE tbproduto ADD CONSTRAINT tbproduto_unidade_medida_check
    CHECK (unidade_medida IN ('UN', 'KG', 'G', 'L', 'ML'));
UPDATE tbproduto SET unidade_medida = 'UN' WHERE natureza = 'INSUMO' AND unidade_medida IS NULL;

-- Ficha técnica: vínculo produto × ingrediente × quantidade utilizada. Os
-- nomes das colunas passam a dizer exatamente isso, e o par
-- (produto, ingrediente) vira único — o mesmo ingrediente repetido na
-- receita duplicaria custo e baixa de estoque.
ALTER TABLE tbfichatecnicaitem RENAME COLUMN insumo_id TO ingrediente_id;
ALTER TABLE tbfichatecnicaitem RENAME COLUMN quantidade TO quantidade_utilizada;

-- Consolida duplicatas já gravadas (soma as quantidades na linha mais antiga)
-- antes de criar a unique.
UPDATE tbfichatecnicaitem f
SET quantidade_utilizada = d.total
FROM (
    SELECT min(id) AS id, sum(quantidade_utilizada) AS total
    FROM tbfichatecnicaitem
    GROUP BY produto_id, ingrediente_id
    HAVING count(*) > 1
) d
WHERE f.id = d.id;

DELETE FROM tbfichatecnicaitem f
USING tbfichatecnicaitem o
WHERE f.produto_id = o.produto_id
  AND f.ingrediente_id = o.ingrediente_id
  AND f.id > o.id;

ALTER TABLE tbfichatecnicaitem
    ADD CONSTRAINT uk_fichatecnicaitem_produto_ingrediente UNIQUE (produto_id, ingrediente_id);
ALTER TABLE tbfichatecnicaitem
    ADD CONSTRAINT ck_fichatecnicaitem_quantidade_utilizada CHECK (quantidade_utilizada > 0);

-- Usado para checar se um ingrediente está em alguma ficha antes de excluí-lo.
CREATE INDEX idx_fichatecnicaitem_ingrediente_id ON tbfichatecnicaitem (ingrediente_id);
