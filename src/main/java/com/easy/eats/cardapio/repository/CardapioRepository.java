package com.easy.eats.cardapio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.cardapio.model.Cardapio;

@Repository
public interface CardapioRepository extends JpaRepository<Cardapio,Integer> {
    
}
