package com.easy.eats.cupom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.cupom.model.CupomUso;

@Repository
public interface CupomUsoRepository extends JpaRepository<CupomUso, Integer> {

    long countByCupomId(Integer cupomId);

    long countByCupomIdAndClienteId(Integer cupomId, Integer clienteId);
}
