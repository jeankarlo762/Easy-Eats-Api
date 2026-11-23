package com.easy.eats.itemVenda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.itemVenda.model.ItemVenda;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Integer> {

    List<ItemVenda> findAllByVenda_Empresa_Id(Integer empresaId);

    Optional<ItemVenda> findByIdAndVenda_Empresa_Id(Integer id, Integer empresaId);

    List<ItemVenda> findAllByVenda_Id(Integer vendaId);
}
