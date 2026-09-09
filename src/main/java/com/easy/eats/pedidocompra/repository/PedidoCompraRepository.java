package com.easy.eats.pedidocompra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.pedidocompra.model.PedidoCompra;

@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Integer> {

    List<PedidoCompra> findAllByEmpresaIdOrderByDtCriacaoDesc(Integer empresaId);

    Optional<PedidoCompra> findByIdAndEmpresaId(Integer id, Integer empresaId);
}
