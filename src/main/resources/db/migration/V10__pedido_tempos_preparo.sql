ALTER TABLE tbpedido
    ADD COLUMN dt_inicio_preparo timestamp(6) without time zone,
    ADD COLUMN dt_pronto timestamp(6) without time zone;
