package com.easyeats.api.integracao.whatsapp;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Cliente HTTP compartilhado pelos provedores, com os timeouts da configuração. */
@Component
public class WhatsAppHttp {

    private final RestTemplate restTemplate;

    public WhatsAppHttp(WhatsAppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeoutMs());
        factory.setReadTimeout(props.getTimeoutMs());
        // Buffering: envia com Content-Length (alguns provedores não aceitam corpo em chunked)
        this.restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
    }

    public RestTemplate cliente() {
        return restTemplate;
    }
}
