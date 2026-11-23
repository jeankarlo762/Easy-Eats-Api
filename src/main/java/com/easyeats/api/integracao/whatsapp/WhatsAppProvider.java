package com.easyeats.api.integracao.whatsapp;

/** Um provedor de envio de mensagens de WhatsApp. Para trocar de API, basta outra implementação. */
public interface WhatsAppProvider {

    /** Identificador usado em {@code integracao.whatsapp.provedor}. */
    String nome();

    /**
     * Envia um texto. {@code numero} já vem normalizado (só dígitos, com DDI). Lança exceção se o provedor
     * recusar o envio; quem chama trata o erro.
     */
    void enviarTexto(String numero, String mensagem);
}
