package com.easyeats.api.repository;

import com.easyeats.api.dto.FiltroLancamentosDto;
import com.easyeats.api.entity.MovimentacaoCaixa;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, Long>,
        JpaSpecificationExecutor<MovimentacaoCaixa>, MovimentacaoCaixaRepositoryCustom {

    // Relatório de fechamento: todos os lançamentos do caixa em ordem cronológica
    List<MovimentacaoCaixa> findByCaixaIdOrderByDataMovimentacaoAscIdAsc(Long caixaId);

    static Specification<MovimentacaoCaixa> comFiltros(FiltroLancamentosDto filtro) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (filtro.inicio() != null) {
                filtros.add(cb.greaterThanOrEqualTo(
                        root.<LocalDateTime>get("dataMovimentacao"), filtro.inicio().atStartOfDay()));
            }
            if (filtro.fim() != null) {
                filtros.add(cb.lessThan(
                        root.<LocalDateTime>get("dataMovimentacao"), filtro.fim().plusDays(1).atStartOfDay()));
            }
            if (filtro.tipo() != null) {
                filtros.add(cb.equal(root.get("tipo"), filtro.tipo()));
            }
            if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
                filtros.add(cb.equal(root.get("usuario"), filtro.usuario()));
            }
            if (filtro.caixaId() != null) {
                filtros.add(cb.equal(root.get("caixa").get("id"), filtro.caixaId()));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }
}
