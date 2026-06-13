package com.easy.eats.mesa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.mesa.model.Mesa;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

}
