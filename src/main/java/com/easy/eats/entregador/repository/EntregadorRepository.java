package com.easy.eats.entregador.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.entregador.model.Entregador;

@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Integer> {

    List<Entregador> findAllByEmpresaId(Integer empresaId);

    Optional<Entregador> findByIdAndEmpresaId(Integer id, Integer empresaId);
}
