package com.easy.eats.estoque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.estoque.model.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque,Integer> {



    
} 
