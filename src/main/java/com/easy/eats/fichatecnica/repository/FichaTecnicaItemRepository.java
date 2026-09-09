package com.easy.eats.fichatecnica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.fichatecnica.model.FichaTecnicaItem;

@Repository
public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Integer> {

    List<FichaTecnicaItem> findAllByProdutoId(Integer produtoId);

    Optional<FichaTecnicaItem> findByIdAndProdutoId(Integer id, Integer produtoId);
}
