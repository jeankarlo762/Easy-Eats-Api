package com.easy.eats.mesa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.mesa.repository.MesaRepository;

@Service
public class MesaService {

    @Autowired
    MesaRepository repository;
}
