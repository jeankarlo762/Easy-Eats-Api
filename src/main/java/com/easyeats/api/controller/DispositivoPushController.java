package com.easyeats.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.easyeats.api.entity.DispositivoPush;
import com.easyeats.api.repository.DispositivoPushRepository;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("/dispositivos")
@RequiredArgsConstructor
@Hidden
public class DispositivoPushController {

    private final DispositivoPushRepository repository;

    public record TokenRequest(String token) {}

    @PostMapping("/push-token")
    public ResponseEntity<Void> registrar(@RequestBody TokenRequest req) {
        String token = req.token();
        boolean valido = token != null
                && (token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken["));
        if (!valido) return ResponseEntity.badRequest().build();

        if (!repository.existsByToken(token)) {
            repository.save(new DispositivoPush(token));
        }
        return ResponseEntity.noContent().build();
    }
}
