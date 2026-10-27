package com.easy.eats.fichatecnica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easy.eats.fichatecnica.model.FichaTecnicaItem;

/**
 * Consultas explícitas: FichaTecnicaItem expõe getProdutoId()/getIngredienteId()
 * para o JSON, e com eles a derivação por nome ("findAllByProdutoId") passaria
 * a procurar um atributo produtoId inexistente em vez de navegar produto.id.
 */
@Repository
public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Integer> {

    @Query("select f from FichaTecnicaItem f where f.produto.id = :produtoId order by f.id")
    List<FichaTecnicaItem> findAllByProdutoId(@Param("produtoId") Integer produtoId);

    @Query("select f from FichaTecnicaItem f where f.id = :id and f.produto.id = :produtoId")
    Optional<FichaTecnicaItem> findByIdAndProdutoId(@Param("id") Integer id, @Param("produtoId") Integer produtoId);

    @Query("select count(f) > 0 from FichaTecnicaItem f "
            + "where f.produto.id = :produtoId and f.ingrediente.id = :ingredienteId")
    boolean existsByProdutoIdAndIngredienteId(@Param("produtoId") Integer produtoId,
            @Param("ingredienteId") Integer ingredienteId);

    /** Fichas técnicas que usam o ingrediente — impede excluí-lo enquanto estiver em uso. */
    @Query("select f from FichaTecnicaItem f where f.ingrediente.id = :ingredienteId")
    List<FichaTecnicaItem> findAllByIngredienteId(@Param("ingredienteId") Integer ingredienteId);
}
