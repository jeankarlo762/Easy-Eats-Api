package com.easyeats.api.integracao.whatsapp;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Gateway próprio com Baileys (pasta whatsapp-gateway deste repositório): o seu número comum de WhatsApp é
 * conectado pelo QR Code e é ele que envia as mensagens. Rotas usadas: POST /send, GET /status, POST /logout,
 * todas com o header x-api-key.
 */
@Component
@RequiredArgsConstructor
public class BaileysProvider implements WhatsAppProvider {

    private final WhatsAppProperties props;
    private final WhatsAppHttp http;

    @Override
    public String nome() {
        return "baileys";
    }

    @Override
    public void enviarTexto(String numero, String mensagem) {
        http.cliente().postForEntity(url("/send"),
                new HttpEntity<>(Map.of("number", numero, "text", mensagem), headers()), String.class);
    }

    /** Situação da conexão: status, número conectado e o texto do QR Code enquanto não conectado. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> status() {
        return http.cliente().exchange(url("/status"), HttpMethod.GET, new HttpEntity<>(headers()), Map.class)
                .getBody();
    }

    /** Desconecta o número; o gateway gera um QR Code novo para conectar outro. */
    public void desconectar() {
        http.cliente().postForEntity(url("/logout"), new HttpEntity<>(Map.of(), headers()), String.class);
    }

    private String url(String rota) {
        if (props.getUrl() == null || props.getUrl().isBlank()) {
            throw new IllegalStateException("Informe WHATSAPP_API_URL com o endereço do gateway Baileys");
        }
        return props.getUrl().replaceAll("/+$", "") + rota;
    }

    private HttpHeaders headers() {
        if (props.getToken() == null || props.getToken().isBlank()) {
            throw new IllegalStateException("Informe WHATSAPP_API_TOKEN com a API_KEY do gateway Baileys");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", props.getToken());
        return headers;
    }
}
