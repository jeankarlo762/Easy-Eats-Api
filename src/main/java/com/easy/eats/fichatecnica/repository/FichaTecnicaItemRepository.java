package com.easy.eats.fichatecnica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easy.eats.fichatecnica.model.FichaTecnicaItem;

@Repository
public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Integer> {

    List<FichaTecnicaItem> findAllByProdutoId(Integer produtoId);

    List<FichaTecnicaItem> findAllByProdutoIdOrderByIdAsc(Integer produtoId);

    Optional<FichaTecnicaItem> findByIdAndProdutoId(Integer id, Integer produtoId);

    boolean existsByProdutoIdAndIngredienteId(Integer produtoId, Integer ingredienteId);

    /** Se o ingrediente compõe a ficha técnica de algum produto ainda ativo (bloqueia a exclusão dele). */
    @Query("select count(f) > 0 from FichaTecnicaItem f where f.ingrediente.id = :ingredienteId "
            + "and (f.produto.flAtivo is null or f.produto.flAtivo = true)")
    boolean ingredienteUsadoEmProdutoAtivo(@Param("ingredienteId") Integer ingredienteId);
}
