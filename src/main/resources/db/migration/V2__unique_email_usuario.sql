-- UsuarioService.salvar() não verificava e-mail duplicado antes de gravar, e
-- sem constraint no banco nada impedia dois usuários com o mesmo e-mail.
-- UsuarioRepository.findByEmail devolve Optional<Usuario> (espera no máximo
-- uma linha) — com duas linhas iguais, o login de qualquer uma das contas
-- passaria a lançar IncorrectResultSizeDataAccessException (500) em vez de
-- autenticar.
ALTER TABLE tbusuario
    ADD CONSTRAINT uk_usuario_email UNIQUE (email);
