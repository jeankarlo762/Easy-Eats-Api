package com.easy.eats.movimentacaoFinanceira.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easy.eats.movimentacaoFinanceira.service.MovimentacaoFinanceiraService;


@Controller
@RequestMapping("/movimentacao_financeira")
public class MovimentacaoFinanceiraController {

    @Autowired
    MovimentacaoFinanceiraService service;

    
}
