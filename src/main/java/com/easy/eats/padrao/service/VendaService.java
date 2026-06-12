package com.easy.eats.padrao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.padrao.repository.VendaRepository;

@Service
public class VendaService {

    @Autowired
    VendaRepository repository;
}
