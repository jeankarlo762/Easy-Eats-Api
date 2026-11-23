package com.easyeats.api.integracao.whatsapp;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Evolution API (https://doc.evolution-api.com): POST {url}/message/sendText/{instancia} com o header apikey.
 * É a API não oficial mais usada no Brasil para conectar um número comum pelo QR Code do WhatsApp.
 */
@Component
@RequiredArgsConstructor
public class EvolutionApiProvider implements WhatsAppProvider {

    private final WhatsAppProperties props;
    private final WhatsAppHttp http;

    @Override
    public String nome() {
        return "evolution";
    }

    @Override
    public void enviarTexto(String numero, String mensagem) {
        if (vazio(props.getUrl()) || vazio(props.getToken()) || vazio(props.getInstancia())) {
            throw new IllegalStateException(
                    "Evolution API incompleta: informe WHATSAPP_API_URL, WHATSAPP_API_TOKEN e WHATSAPP_INSTANCIA");
        }
        String url = props.getUrl().replaceAll("/+$", "") + "/message/sendText/" + props.getInstancia();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", props.getToken());
        http.cliente().postForEntity(url, new HttpEntity<>(Map.of("number", numero, "text", mensagem), headers),
                String.class);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
