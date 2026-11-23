package com.easyeats.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("/sec")
@Hidden
public class TesteSecurityController {
    @GetMapping("/publico/hello")
    public String publico() {
        return "Olá, visitante!";
    }
    @GetMapping("/user/hello")
    public String user() {
        return "Olá, usuário autenticado!";
    }
    @GetMapping("/admin/hello")
    public String admin() {
        return "Olá, administrador!";
    }
}
