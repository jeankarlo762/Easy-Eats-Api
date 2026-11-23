package com.easyeats.api.integracao.whatsapp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import com.easyeats.api.security.UsuarioAutenticado;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/integracoes/whatsapp")
@RequiredArgsConstructor
@Tag(name = "Integração WhatsApp", description = "Situação e teste do envio de WhatsApp (Administrador e Gerente). Conexão do número pelo QR Code (Baileys). A configuração vem das variáveis de ambiente WHATSAPP_*.")
@SecurityRequirement(name = "bearerAuth")
public class WhatsAppController {

    private final WhatsAppProperties props;
    private final WhatsAppService whatsApp;
    private final BaileysProvider baileys;

    public record TesteWhatsAppDto(@NotBlank(message = "Informe o telefone") String telefone) {
    }

    @Operation(summary = "Situação da integração", description = "Mostra o provedor e o que está configurado, sem expor o token.")
    @GetMapping("/status")
    public Map<String, Object> status(Authentication authentication) {
        exigirGestao(authentication);
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("habilitado", props.isHabilitado());
        status.put("provedor", props.getProvedor());
        status.put("urlConfigurada", props.getUrl() != null && !props.getUrl().isBlank());
        status.put("tokenConfigurado", props.getToken() != null && !props.getToken().isBlank());
        status.put("numeroLojaConfigurado", props.getNumeroLoja() != null && !props.getNumeroLoja().isBlank());
        status.put("notificarCliente", props.isNotificarCliente());
        return status;
    }

    @Operation(summary = "Conexão do número (Baileys)", description = "Status da conexão, número conectado e o texto do QR Code a escanear enquanto não estiver conectado.")
    @GetMapping("/conexao")
    public ResponseEntity<Map<String, Object>> conexao(Authentication authentication) {
        exigirGestao(authentication);
        if (!props.isHabilitado()) {
            return ResponseEntity.ok(Map.of("status", "desabilitado"));
        }
        try {
            return ResponseEntity.ok(baileys.status());
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("status", "indisponivel",
                    "erro", "Não foi possível falar com o gateway do WhatsApp: " + e.getMessage()));
        }
    }

    @Operation(summary = "Desconectar o número", description = "Desconecta o WhatsApp atual; um QR Code novo é gerado para conectar outro número.")
    @PostMapping("/desconectar")
    public ResponseEntity<Map<String, Object>> desconectar(Authentication authentication) {
        exigirGestao(authentication);
        try {
            baileys.desconectar();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("erro", String.valueOf(e.getMessage())));
        }
    }

    @Operation(summary = "Enviar mensagem de teste", description = "Envia agora uma mensagem de teste ao telefone informado e devolve o erro do provedor, se houver.")
    @PostMapping("/teste")
    public ResponseEntity<Map<String, Object>> testar(@Valid @RequestBody TesteWhatsAppDto dto,
            Authentication authentication) {
        exigirGestao(authentication);
        try {
            whatsApp.enviarAgoraOuFalhar(dto.telefone(), "✅ Teste da integração de WhatsApp do Easy Eats.");
            return ResponseEntity.ok(Map.of("enviado", true, "provedor",
                    props.isHabilitado() ? props.getProvedor() : "log (integração desabilitada)"));
        } catch (HttpStatusCodeException e) {
            // erro devolvido pelo gateway, ex.: número não conectado ou sem WhatsApp
            Map<?, ?> corpo = e.getResponseBodyAs(Map.class);
            Object erro = corpo != null && corpo.get("erro") != null ? corpo.get("erro") : e.getStatusText();
            return ResponseEntity.status(502).body(Map.of("enviado", false, "erro", String.valueOf(erro)));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("enviado", false, "erro", String.valueOf(e.getMessage())));
        }
    }

    private static void exigirGestao(Authentication authentication) {
        if (!UsuarioAutenticado.de(authentication).gestao()) {
            throw new AccessDeniedException("Operação permitida apenas para Administrador ou Gerente");
        }
    }
}
