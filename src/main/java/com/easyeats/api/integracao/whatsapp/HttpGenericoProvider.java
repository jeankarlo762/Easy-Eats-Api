package com.easyeats.api.integracao.whatsapp;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Provedor genérico: POST {url} com JSON {"number": "...", "text": "..."} e, se houver token,
 * "Authorization: Bearer {token}". Serve para Z-API, WPPConnect, um webhook do n8n etc. (direto ou via adaptador).
 */
@Component
@RequiredArgsConstructor
public class HttpGenericoProvider implements WhatsAppProvider {

    private final WhatsAppProperties props;
    private final WhatsAppHttp http;

    @Override
    public String nome() {
        return "http";
    }

    @Override
    public void enviarTexto(String numero, String mensagem) {
        if (props.getUrl() == null || props.getUrl().isBlank()) {
            throw new IllegalStateException("Informe WHATSAPP_API_URL para o provedor http");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (props.getToken() != null && !props.getToken().isBlank()) {
            headers.setBearerAuth(props.getToken());
        }
        http.cliente().postForEntity(props.getUrl(),
                new HttpEntity<>(Map.of("number", numero, "text", mensagem), headers), String.class);
    }
}
