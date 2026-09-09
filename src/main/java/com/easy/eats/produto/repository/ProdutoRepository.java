package com.easy.eats.produto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.produto.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findAllByEmpresaId(Integer empresaId);

    Optional<Produto> findByIdAndEmpresaId(Integer id, Integer empresaId);

    boolean existsByCategoriaId(Integer categoriaId);
}