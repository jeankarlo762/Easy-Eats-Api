package com.easy.eats.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origin:}")
    private String corsAllowedOrigin;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Padrão de porta livre para o servidor de dev do Angular (ng serve
        // pode subir em 4200, 5173 etc. dependendo do que está livre na
        // máquina), mais a origem de produção configurada via
        // app.cors.allowed-origin (necessária para o cardápio/checkout
        // público, servido de um domínio real).
        List<String> origensPermitidas = new ArrayList<>(List.of("http://localhost:*"));
        if (corsAllowedOrigin != null && !corsAllowedOrigin.isBlank()) {
            origensPermitidas.add(corsAllowedOrigin);
        }
        configuration.setAllowedOriginPatterns(origensPermitidas);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Rotas públicas do link do cardápio (pedido/checkout do cliente final) e
                        // do callback de retorno do onboarding Stripe Connect — sem JWT. O
                        // JwtAuthenticationFilter não quebra requests sem header Authorization,
                        // então não precisa de ajuste no filtro para liberar isso.
                        .requestMatchers("/public/**").permitAll()
                        // Precisam vir antes do matcher genérico de /empresa/** (SUPERADMIN):
                        // o administrador da própria empresa pode editar o slug do link público
                        // e os dados do próprio estabelecimento (tela de Configurações) sem
                        // precisar das permissões amplas de SUPERADMIN sobre Empresa.
                        .requestMatchers(HttpMethod.PUT, "/empresa/*/slug").hasAnyRole("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers("/empresa/minha-empresa").hasAnyRole("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers("/empresa/**").hasRole("SUPERADMIN")
                        .requestMatchers("/segmentos/**").hasRole("SUPERADMIN")
                        .requestMatchers("/usuarios/**").hasAnyRole("SUPERADMIN", "ADMINISTRADOR")
                        // Frente de Caixa: todos os perfis autenticados podem ver o status
                        // (GET, cai em anyRequest().authenticated() abaixo), mas só
                        // ADMINISTRADOR/OPERADOR abrem, fecham ou lançam sangria/suprimento.
                        .requestMatchers(HttpMethod.POST, "/caixa/abrir").hasAnyRole("ADMINISTRADOR", "OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/caixa/*/fechar").hasAnyRole("ADMINISTRADOR", "OPERADOR")
                        .requestMatchers(HttpMethod.POST, "/caixa/*/movimentacao")
                        .hasAnyRole("ADMINISTRADOR", "OPERADOR")
                        .anyRequest().authenticated())
                // Sem isso o Spring Security usa o Http403ForbiddenEntryPoint e
                // responde 403 tanto para "não autenticado" (token ausente ou
                // expirado) quanto para "sem permissão", impossibilitando o
                // frontend distinguir os dois casos — ele acabava derrubando a
                // sessão do usuário em qualquer erro de permissão.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, erro) -> escreverErro(response,
                                HttpStatus.UNAUTHORIZED, "Sessão expirada ou inválida. Faça login novamente."))
                        .accessDeniedHandler((request, response, erro) -> escreverErro(response,
                                HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.")))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Mesmo formato de corpo do GlobalExceptionHandler
     * ({@code {timestamp, status, erro, mensagem}}), para o frontend conseguir
     * ler a mensagem com o mesmo utilitário em qualquer erro da API.
     */
    private void escreverErro(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);

        new ObjectMapper().writeValue(response.getOutputStream(), corpo);
    }
}
