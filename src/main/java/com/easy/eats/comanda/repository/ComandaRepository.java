package com.easy.eats.comanda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.comanda.enums.StatusComanda;
import com.easy.eats.comanda.model.Comanda;

@Repository
public interface ComandaRepository extends JpaRepository<Comanda, Integer> {

    List<Comanda> findAllByEmpresaId(Integer empresaId);

    List<Comanda> findAllByEmpresaIdAndStatus(Integer empresaId, StatusComanda status);

    Optional<Comanda> findByIdAndEmpresaId(Integer id, Integer empresaId);

    Optional<Comanda> findTopByEmpresaIdOrderByNumeroDesc(Integer empresaId);
}
