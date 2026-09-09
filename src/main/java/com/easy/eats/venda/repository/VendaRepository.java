package com.easy.eats.venda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.venda.model.Venda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

    List<Venda> findAllByEmpresaId(Integer empresaId);

    Optional<Venda> findByIdAndEmpresaId(Integer id, Integer empresaId);

    /** Histórico de compras do cliente — base para fidelidade e cashback. */
    List<Venda> findAllByCliente_IdAndEmpresaId(Integer clienteId, Integer empresaId);
}
