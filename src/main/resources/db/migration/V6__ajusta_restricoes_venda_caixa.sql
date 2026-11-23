-- Ajusta restricoes de bancos criados antes da venda/caixa. O Hibernate (ddl-auto=update) cria tabelas e
-- colunas, mas nao altera restricoes CHECK nem NOT NULL ja existentes. Os blocos so agem se a tabela existir,
-- entao a migration e inofensiva em um banco novo (onde o Hibernate ja gera o schema correto).

-- 1) tb_movimentacao_caixa.tipo passa a aceitar VENDA (entrada de dinheiro gerada ao finalizar a venda).
DO $$
BEGIN
    IF to_regclass('public.tb_movimentacao_caixa') IS NOT NULL THEN
        ALTER TABLE tb_movimentacao_caixa DROP CONSTRAINT IF EXISTS tb_movimentacao_caixa_tipo_check;
        ALTER TABLE tb_movimentacao_caixa ADD CONSTRAINT tb_movimentacao_caixa_tipo_check
            CHECK (tipo IN ('FUNDO_INICIAL', 'SUPRIMENTO', 'SANGRIA', 'VENDA'));
    END IF;
END $$;

-- 2) tb_pedido.mesa_id passa a ser opcional (comanda de retirada nao tem mesa).
DO $$
BEGIN
    IF to_regclass('public.tb_pedido') IS NOT NULL THEN
        ALTER TABLE tb_pedido ALTER COLUMN mesa_id DROP NOT NULL;
    END IF;
END $$;