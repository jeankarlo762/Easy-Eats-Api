package com.easy.eats.pagamento.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.pagamento.repository.PagamentoRepository;

@Service
public class PagamentoService {

    @Autowired
    PagamentoRepository repository;
}
