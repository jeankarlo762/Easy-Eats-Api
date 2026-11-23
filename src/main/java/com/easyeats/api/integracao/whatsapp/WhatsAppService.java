package com.easyeats.api.integracao.whatsapp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.PreDestroy;

/**
 * Ponto único de envio de WhatsApp. O envio é feito em segundo plano e só depois do commit da transação, então
 * uma falha (ou lentidão) do provedor nunca desfaz nem atrasa o pedido: o erro vai para o log.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppProperties props;
    private final Map<String, WhatsAppProvider> provedores;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "whatsapp-envio");
        thread.setDaemon(true);
        return thread;
    });

    public WhatsAppService(WhatsAppProperties props, List<WhatsAppProvider> provedores) {
        this.props = props;
        this.provedores = provedores.stream()
                .collect(Collectors.toMap(WhatsAppProvider::nome, Function.identity()));
    }

    public boolean habilitado() {
        return props.isHabilitado();
    }

    public String provedorConfigurado() {
        return props.getProvedor();
    }

    /** Agenda o envio (após o commit, se houver transação). Nunca lança exceção. */
    public void enviar(String telefone, String mensagem) {
        String numero = normalizar(telefone);
        if (numero == null) {
            log.warn("WhatsApp não enviado: telefone inválido '{}'", telefone);
            return;
        }
        Runnable envio = () -> executor.submit(() -> enviarAgora(numero, mensagem));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envio.run();
                }
            });
        } else {
            envio.run();
        }
    }

    /** Envio síncrono, usado pelo teste da integração. Lança a exceção do provedor. */
    public void enviarAgoraOuFalhar(String telefone, String mensagem) {
        String numero = normalizar(telefone);
        if (numero == null) {
            throw new IllegalArgumentException("Telefone inválido");
        }
        provedor().enviarTexto(numero, mensagem);
    }

    private void enviarAgora(String numero, String mensagem) {
        try {
            provedor().enviarTexto(numero, mensagem);
            log.info("WhatsApp enviado para {}", mascarar(numero));
        } catch (Exception e) {
            log.warn("Falha ao enviar WhatsApp para {}: {}", mascarar(numero), e.getMessage());
        }
    }

    private WhatsAppProvider provedor() {
        String nome = props.isHabilitado() ? props.getProvedor() : "log";
        WhatsAppProvider provedor = provedores.get(nome == null ? "" : nome.trim().toLowerCase());
        if (provedor == null) {
            throw new IllegalStateException("Provedor de WhatsApp desconhecido: " + nome
                    + " (use evolution, http ou log)");
        }
        return provedor;
    }

    /** Só dígitos, com DDI. 10 ou 11 dígitos (DDD + número) recebem o DDI padrão. */
    String normalizar(String telefone) {
        if (telefone == null) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = props.getDdiPadrao() + digitos;
        }
        return digitos.length() >= 12 && digitos.length() <= 15 ? digitos : null;
    }

    private static String mascarar(String numero) {
        return numero.length() <= 4 ? "****" : "****" + numero.substring(numero.length() - 4);
    }

    @PreDestroy
    void encerrar() {
        executor.shutdown();
    }
}
