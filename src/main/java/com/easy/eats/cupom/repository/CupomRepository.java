package com.easy.eats.cupom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.cupom.model.Cupom;

@Repository
public interface CupomRepository extends JpaRepository<Cupom, Integer> {

    List<Cupom> findAllByEmpresaId(Integer empresaId);

    Optional<Cupom> findByIdAndEmpresaId(Integer id, Integer empresaId);

    Optional<Cupom> findByCodigoIgnoreCaseAndEmpresaId(String codigo, Integer empresaId);
}
