package com.easy.eats.pagamento.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easy.eats.pagamento.service.PagamentoService;

@Controller
@RequestMapping("/pagamento")
public class PagamentoController {

    @Autowired
    PagamentoService service;

    
}
