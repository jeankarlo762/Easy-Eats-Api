package com.easy.eats.entrega.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.entrega.model.Entrega;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Integer> {

    List<Entrega> findAllByEmpresaIdOrderByDtCriacaoDesc(Integer empresaId);

    Optional<Entrega> findByIdAndEmpresaId(Integer id, Integer empresaId);
}
