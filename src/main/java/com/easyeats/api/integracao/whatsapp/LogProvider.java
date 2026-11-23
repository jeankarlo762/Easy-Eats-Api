package com.easyeats.api.integracao.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Não envia nada: só registra a mensagem no log. Útil em desenvolvimento e testes. */
@Component
public class LogProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(LogProvider.class);

    @Override
    public String nome() {
        return "log";
    }

    @Override
    public void enviarTexto(String numero, String mensagem) {
        log.info("[WhatsApp/log] para {}:\n{}", numero, mensagem);
    }
}
