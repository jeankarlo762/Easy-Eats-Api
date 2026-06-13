package com.easy.eats.mesa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easy.eats.mesa.service.MesaService;


@Controller
@RequestMapping("/mesa")
public class MesaController {

    @Autowired
    MesaService service;

    
}
