package com.easy.eats.despesa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.despesa.model.Despesa;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Integer> {

    List<Despesa> findAllByEmpresaIdOrderByDtDespesaDesc(Integer empresaId);

    Optional<Despesa> findByIdAndEmpresaId(Integer id, Integer empresaId);
}
