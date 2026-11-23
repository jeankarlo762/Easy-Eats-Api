package com.easyeats.api.controller;

import com.easyeats.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login do sistema. A rota é pública: devolve o token JWT que deve ser usado nas demais rotas, pelo botão Authorize.")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Entrar no sistema", description = "Valida o usuário e a senha e devolve o token JWT em texto. Use o token no botão Authorize, no topo da página, para chamar as outras rotas. Usuário e senha são enviados como parâmetros da requisição, e não no corpo.")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. O corpo da resposta é o token JWT")
    @ApiResponse(responseCode = "403", description = "Usuário ou senha inválidos")
    @PostMapping("/login")
    public String login(
            @Parameter(description = "Nome de usuário", example = "superadmin") @RequestParam String username,
            @Parameter(description = "Senha do usuário") @RequestParam String password
    ) {
        log.info("Tentativa de login para usuário: {}", username);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                password
                        )
                );

        log.info("Autenticação realizada com sucesso");

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        return jwtUtil.generateToken(userDetails);
    }
}
