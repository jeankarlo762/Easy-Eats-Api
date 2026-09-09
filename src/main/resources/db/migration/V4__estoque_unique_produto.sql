-- O módulo de estoque rodava sobre uma árvore binária em memória
-- (ArvoreInsumos), sem persistência real, sem isolamento por empresa e
-- perdendo tudo a cada restart — mesmo já existindo a entidade Estoque e o
-- EstoqueRepository, nunca usados por nenhum controller. Agora
-- EstoqueController/Service persistem de verdade nesta tabela, um registro
-- por produto.
ALTER TABLE tbestoque
    ADD CONSTRAINT uk_estoque_produto UNIQUE (produto_id);
