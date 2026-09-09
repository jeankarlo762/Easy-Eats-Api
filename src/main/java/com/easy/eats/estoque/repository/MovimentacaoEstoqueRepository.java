package com.easy.eats.estoque.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easy.eats.estoque.model.ItemMaisConsumido;
import com.easy.eats.estoque.model.MovimentacaoEstoque;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {

    List<MovimentacaoEstoque> findAllByEmpresaIdOrderByDtMovimentacaoDesc(Integer empresaId, Pageable pageable);

    @Query("select new com.easy.eats.estoque.model.ItemMaisConsumido(m.produto.nome, sum(m.quantidade)) "
            + "from MovimentacaoEstoque m "
            + "where m.empresa.id = :empresaId and m.tipo = com.easy.eats.estoque.enums.TipoMovimentacaoEstoque.SAIDA "
            + "group by m.produto.nome "
            + "order by sum(m.quantidade) desc")
    List<ItemMaisConsumido> itensMaisConsumidos(@Param("empresaId") Integer empresaId, Pageable pageable);
}
