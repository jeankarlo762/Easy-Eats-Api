package com.easy.eats.itemVenda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easy.eats.itemVenda.service.ItemVendaService;

@Controller
@RequestMapping("/item-venda")
public class ItemVendaController {

    @Autowired
    ItemVendaService service;

    
}
