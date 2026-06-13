package com.easy.eats.venda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.venda.model.Venda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

}
