package com.easyeats.api.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig {

    private static final String DESCRICAO = """
            API do **Easy Eats**, sistema de gestão de restaurante: cardápio, estoque, comandas, caixa e vendas.

            **Como usar**
            1. Em **Autenticação**, execute `POST /auth/login` com usuário e senha e copie o token devolvido.
            2. Clique em **Authorize**, no topo da página, cole o token e confirme.
            3. As demais rotas passam a enviar o token automaticamente.

            **Fluxo do atendimento**
            abrir o caixa, lançar a comanda de uma mesa (a venda da mesa abre sozinha), enviar para preparo,
            registrar os pagamentos e finalizar a venda, que libera a mesa e lança o dinheiro no caixa.

            **Erros**
            as respostas de erro trazem o campo `erro` com a mensagem.
            """;

    /** Ordem das seções na tela, seguindo o fluxo do sistema. */
    private static final List<String> ORDEM_DAS_SECOES = List.of(
            "Autenticação",
            "Mesas",
            "Categorias",
            "Produtos",
            "Ingredientes",
            "Ficha técnica",
            "Preços",
            "Estoque",
            "Pedidos (comandas)",
            "Itens do pedido",
            "Caixa",
            "Vendas");

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Easy Eats")
                        .version("v1.0.0")
                        .description(DESCRICAO))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira seu token JWT")));
    }

    /** Junta as seções repetidas e as coloca na ordem do fluxo; seções não listadas vão para o fim. */
    @Bean
    public OpenApiCustomizer ordenarSecoes() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            Map<String, Tag> unicas = new LinkedHashMap<>();
            for (Tag tag : openApi.getTags()) {
                unicas.merge(tag.getName(), tag, (atual, nova) -> atual.getDescription() != null ? atual : nova);
            }
            List<Tag> ordenadas = new java.util.ArrayList<>();
            ORDEM_DAS_SECOES.stream().filter(unicas::containsKey).forEach(nome -> ordenadas.add(unicas.remove(nome)));
            ordenadas.addAll(unicas.values());
            openApi.setTags(ordenadas);
        };
    }
}