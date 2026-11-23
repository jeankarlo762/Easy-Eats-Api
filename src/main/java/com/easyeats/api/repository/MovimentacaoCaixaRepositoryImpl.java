package com.easyeats.api.repository;

import com.easyeats.api.dto.TotalPorTipoDto;
import com.easyeats.api.entity.MovimentacaoCaixa;
import com.easyeats.api.entity.TipoMovimentacaoCaixaEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class MovimentacaoCaixaRepositoryImpl implements MovimentacaoCaixaRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<TotalPorTipoDto> totaisPorTipo(Specification<MovimentacaoCaixa> filtro) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<MovimentacaoCaixa> root = query.from(MovimentacaoCaixa.class);

        Predicate predicado = filtro.toPredicate(root, query, cb);
        if (predicado != null) {
            query.where(predicado);
        }
        query.multiselect(
                        root.get("tipo").alias("tipo"),
                        cb.sum(root.<BigDecimal>get("valor")).alias("total"))
                .groupBy(root.get("tipo"));

        return entityManager.createQuery(query).getResultList().stream()
                .map(linha -> new TotalPorTipoDto(
                        linha.get("tipo", TipoMovimentacaoCaixaEnum.class),
                        linha.get("total", BigDecimal.class)))
                .toList();
    }
}
