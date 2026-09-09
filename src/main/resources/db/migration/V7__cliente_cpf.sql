-- O frontend já tinha um campo de CPF no formulário de clientes desde
-- sempre (tela mock), mas o backend nunca teve essa coluna — o valor era
-- só descartado.
ALTER TABLE tbcliente
    ADD COLUMN cpf character varying(255);
