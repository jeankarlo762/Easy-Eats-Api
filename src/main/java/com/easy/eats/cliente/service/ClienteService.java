package com.easy.eats.cliente.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.cliente.repository.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository repository;
}
