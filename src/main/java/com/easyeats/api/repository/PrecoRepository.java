package com.easyeats.api.repository;

import com.easyeats.api.entity.Preco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PrecoRepository
        extends JpaRepository<Preco, Long> {

    List<Preco> findByProdutoId(Long produtoId);

    Optional<Preco>
    findFirstByProdutoIdAndFlativoOrderByIdDesc(
            Long produtoId,
            String flativo
    );

    // Cardápio digital: preços ativos de produtos e categorias ativos, com produto e categoria carregados juntos
    @Query("select p from Preco p join fetch p.produto pr join fetch pr.categoria c "
            + "where p.flativo = 'S' and pr.flativo = 'S' and c.flativo = 'S' "
            + "order by c.nome, pr.nome, p.id desc")
    List<Preco> buscarPrecosAtivosDoCardapio();
}
