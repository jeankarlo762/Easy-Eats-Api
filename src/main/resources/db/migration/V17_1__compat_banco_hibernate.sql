-- Compatibilidade com bancos criados pelo Hibernate (ddl-auto=update) a partir
-- do repositório joao-vitor1007/Easy-Eats-Api, como o banco atual do Railway.
-- Lá algumas colunas têm outro nome/tipo (data_criacao timestamp em vez de
-- dt_criacao texto) e faltam colunas que as entidades esperam
-- (ddl-auto=validate). Em bancos criados pelo Flyway as colunas já existem e
-- os UPDATEs não rodam, então isto não faz nada.
ALTER TABLE tbvenda
    ADD COLUMN IF NOT EXISTS dt_fechamento character varying(255),
    ADD COLUMN IF NOT EXISTS dt_criacao character varying(255),
    ADD COLUMN IF NOT EXISTS dt_alteracao character varying(255);

ALTER TABLE tbmovimentacao_financeira
    ADD COLUMN IF NOT EXISTS dt_alteracao character varying(255);

-- Copia as datas das colunas antigas no mesmo formato que o código grava
-- (LocalDateTime.toString(), ex.: 2026-10-29T23:08:31.123456).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'tbvenda' AND column_name = 'data_criacao') THEN
        UPDATE tbvenda
           SET dt_criacao = to_char(data_criacao, 'YYYY-MM-DD"T"HH24:MI:SS.US')
         WHERE dt_criacao IS NULL AND data_criacao IS NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'tbvenda' AND column_name = 'data_alteracao') THEN
        UPDATE tbvenda
           SET dt_alteracao = to_char(data_alteracao, 'YYYY-MM-DD"T"HH24:MI:SS.US')
         WHERE dt_alteracao IS NULL AND data_alteracao IS NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'tbmovimentacao_financeira' AND column_name = 'data_movimentacao') THEN
        UPDATE tbmovimentacao_financeira
           SET dt_alteracao = to_char(data_movimentacao, 'YYYY-MM-DD"T"HH24:MI:SS.US')
         WHERE dt_alteracao IS NULL AND data_movimentacao IS NOT NULL;
    END IF;
END $$;
