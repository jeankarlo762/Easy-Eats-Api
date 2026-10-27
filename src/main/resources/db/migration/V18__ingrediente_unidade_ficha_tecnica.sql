-- Unidade de medida do ingrediente (Produto natureza INSUMO). Nula para os
-- produtos já existentes e para os que não são ingredientes.
ALTER TABLE tbproduto
    ADD COLUMN unidade_medida character varying(10);

ALTER TABLE tbproduto
    ADD CONSTRAINT tbproduto_unidade_medida_check
    CHECK (unidade_medida IS NULL OR unidade_medida IN ('UN', 'KG', 'G', 'L', 'ML'));

-- Ficha técnica: deixa explícito o vínculo produto × ingrediente × quantidade
-- utilizada, que é a base para custo, planejamento de produção e baixa
-- automática de estoque.
ALTER TABLE tbfichatecnicaitem RENAME COLUMN insumo_id TO ingrediente_id;
ALTER TABLE tbfichatecnicaitem RENAME COLUMN quantidade TO quantidade_utilizada;

-- Um ingrediente aparece no máximo uma vez por produto. Linhas repetidas que
-- já existam são consolidadas (somando a quantidade) antes da constraint.
UPDATE tbfichatecnicaitem f
SET quantidade_utilizada = d.total
FROM (
    SELECT MIN(id) AS id, SUM(quantidade_utilizada) AS total
    FROM tbfichatecnicaitem
    GROUP BY produto_id, ingrediente_id
    HAVING COUNT(*) > 1
) d
WHERE f.id = d.id;

DELETE FROM tbfichatecnicaitem f
USING tbfichatecnicaitem outro
WHERE f.produto_id = outro.produto_id
  AND f.ingrediente_id = outro.ingrediente_id
  AND f.id > outro.id;

ALTER TABLE tbfichatecnicaitem
    ADD CONSTRAINT uk_fichatecnicaitem_produto_ingrediente UNIQUE (produto_id, ingrediente_id);

ALTER TABLE tbfichatecnicaitem
    ADD CONSTRAINT ck_fichatecnicaitem_quantidade_positiva CHECK (quantidade_utilizada > 0);

CREATE INDEX idx_fichatecnicaitem_ingrediente_id ON tbfichatecnicaitem (ingrediente_id);
