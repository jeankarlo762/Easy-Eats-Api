package com.easy.eats.integracao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.integracao.model.IntegracaoConfig;

@Repository
public interface IntegracaoConfigRepository extends JpaRepository<IntegracaoConfig, Integer> {

    List<IntegracaoConfig> findAllByEmpresaId(Integer empresaId);

    Optional<IntegracaoConfig> findByEmpresaIdAndChave(Integer empresaId, String chave);
}
