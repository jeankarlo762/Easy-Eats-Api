package com.easy.eats.movimentacaoFinanceira.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.movimentacaoFinanceira.repository.MovimentacaoFinanceiraRepository;

@Service
public class MovimentacaoFinanceiraService {

    @Autowired
    MovimentacaoFinanceiraRepository repository;
}
