package com.easy.eats.estoque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.produto.enums.NaturezaProduto;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {

    List<Estoque> findAllByProduto_EmpresaId(Integer empresaId);

    Optional<Estoque> findByProdutoId(Integer produtoId);

    Optional<Estoque> findByProdutoIdAndProduto_EmpresaId(Integer produtoId, Integer empresaId);

    /** Ingredientes = INSUMOs com registro de estoque. */
    List<Estoque> findAllByProduto_EmpresaIdAndProduto_NaturezaOrderByProduto_NomeAsc(Integer empresaId,
            NaturezaProduto natureza);

    List<Estoque> findAllByProduto_NaturezaOrderByProduto_NomeAsc(NaturezaProduto natureza);

    /** Insumos cuja quantidade atual já caiu abaixo do mínimo configurado. */
    @Query("select e from Estoque e where e.produto.empresa.id = :empresaId "
            + "and e.quantidadeAtual < e.estoqueMinimo")
    List<Estoque> findAbaixoDoMinimo(@Param("empresaId") Integer empresaId);
}
