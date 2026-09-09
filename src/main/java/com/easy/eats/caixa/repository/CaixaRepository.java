package com.easy.eats.caixa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.caixa.enums.StatusCaixa;
import com.easy.eats.caixa.model.Caixa;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Integer> {

    List<Caixa> findAllByEmpresaId(Integer empresaId);

    Optional<Caixa> findByIdAndEmpresaId(Integer id, Integer empresaId);

    Optional<Caixa> findByEmpresaIdAndStatus(Integer empresaId, StatusCaixa status);
}
