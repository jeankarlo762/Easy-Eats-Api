package com.easyeats.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/**
 * Usuário da requisição atual, extraído do token. {@code gestao} indica perfil de
 * Administrador ou Gerente.
 */
public record UsuarioAutenticado(String username, boolean gestao) {

    private static final Set<String> AUTORIDADES_GESTAO = Set.of("ROLE_ADMIN", "ROLE_GERENTE");

    public static UsuarioAutenticado de(Authentication authentication) {
        boolean gestao = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AUTORIDADES_GESTAO::contains);
        return new UsuarioAutenticado(authentication.getName(), gestao);
    }
}
