package com.easyeats.api.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyeats.api.dto.CardapioPublicoDto;
import com.easyeats.api.dto.PedidoCardapioCriadoDto;
import com.easyeats.api.dto.PedidoCardapioDto;
import com.easyeats.api.service.CardapioDigitalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Rotas públicas (sem login) usadas pela página aberta pelo QR Code. */
@RestController
@RequestMapping("/publico/cardapio")
@RequiredArgsConstructor
@Tag(name = "Cardápio digital (público)", description = "Cardápio aberto pelo QR Code, sem login. O cliente escolhe os produtos, informa nome, telefone, endereço e forma de pagamento e envia o pedido, que cai na cozinha como comanda ABERTA.")
public class CardapioPublicoController {

    // Limite simples contra abuso da rota pública: pedidos por IP numa janela de tempo
    private static final Duration JANELA = Duration.ofMinutes(10);

    private final CardapioDigitalService service;

    @Value("${cardapio.limite-pedidos-por-ip:5}")
    private int limitePedidos;
    private final Map<String, Deque<Instant>> pedidosPorIp = new ConcurrentHashMap<>();

    @Operation(summary = "Ver cardápio", description = "Produtos ativos com preço ativo, agrupados por categoria, e as formas de pagamento aceitas.")
    @GetMapping
    public CardapioPublicoDto cardapio() {
        return service.cardapio();
    }

    @Operation(summary = "Fazer pedido", description = "Cria a venda (DELIVERY ou RETIRADA) e a comanda ABERTA. O preço vem do preço ativo de cada produto. Envia a confirmação por WhatsApp.")
    @ApiResponse(responseCode = "201", description = "Pedido recebido")
    @ApiResponse(responseCode = "422", description = "Endereço, telefone, estoque ou forma de pagamento inválidos")
    @ApiResponse(responseCode = "429", description = "Muitos pedidos seguidos do mesmo aparelho")
    @PostMapping("/pedidos")
    public ResponseEntity<?> fazerPedido(@Valid @RequestBody PedidoCardapioDto dto, HttpServletRequest request) {
        String ip = ip(request);
        if (excedeuLimite(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("erro", "Muitos pedidos em pouco tempo. Aguarde alguns minutos e tente de novo."));
        }
        PedidoCardapioCriadoDto criado = service.fazerPedido(dto);
        registrar(ip);   // só pedidos aceitos contam: erros de preenchimento não bloqueiam o cliente
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    private boolean excedeuLimite(String ip) {
        Deque<Instant> registros = pedidosPorIp.get(ip);
        if (registros == null) {
            return false;
        }
        synchronized (registros) {
            descartarAntigos(registros);
            return registros.size() >= limitePedidos;
        }
    }

    private void registrar(String ip) {
        Deque<Instant> registros = pedidosPorIp.computeIfAbsent(ip, chave -> new ArrayDeque<>());
        synchronized (registros) {
            descartarAntigos(registros);
            registros.addLast(Instant.now());
        }
        if (pedidosPorIp.size() > 10_000) {
            pedidosPorIp.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    descartarAntigos(e.getValue());
                    return e.getValue().isEmpty();
                }
            });
        }
    }

    private static void descartarAntigos(Deque<Instant> registros) {
        Instant limite = Instant.now().minus(JANELA);
        while (!registros.isEmpty() && registros.peekFirst().isBefore(limite)) {
            registros.pollFirst();
        }
    }

    // Atrás do proxy do Railway, o IP do cliente vem no X-Forwarded-For
    private static String ip(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
