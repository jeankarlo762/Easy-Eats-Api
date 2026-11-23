package com.easyeats.api.integracao.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuração da integração com WhatsApp (prefixo {@code integracao.whatsapp}). Nada é fixo no código: em
 * produção os valores vêm das variáveis de ambiente WHATSAPP_* (ver application.properties).
 */
@Data
@Component
@ConfigurationProperties(prefix = "integracao.whatsapp")
public class WhatsAppProperties {

    /** Liga/desliga o envio. Desligado, as mensagens só aparecem no log. */
    private boolean habilitado = false;

    /** Provedor: {@code evolution} (Evolution API), {@code http} (genérico) ou {@code log} (só registra no log). */
    private String provedor = "evolution";

    /**
     * Evolution: URL base do servidor (ex.: https://evolution.seudominio.com).
     * HTTP genérico: URL completa do endpoint que recebe o POST.
     */
    private String url;

    /** Evolution: apikey da instância. HTTP genérico: enviado como "Authorization: Bearer". */
    private String token;

    /** Evolution: nome da instância conectada (o número que envia as mensagens). */
    private String instancia;

    /** Número da loja que recebe o aviso de novo pedido (com DDI e DDD, ex.: 5511999998888). Opcional. */
    private String numeroLoja;

    /** Envia ao cliente a confirmação do pedido no telefone que ele informou. */
    private boolean notificarCliente = true;

    /** DDI acrescentado quando o telefone vem sem ele (10 ou 11 dígitos). */
    private String ddiPadrao = "55";

    /** Tempo máximo de conexão e de resposta do provedor, em milissegundos. */
    private int timeoutMs = 10000;
}
