package com.easyeats.api.repository;

import com.easyeats.api.entity.Caixa;
import com.easyeats.api.entity.StatusCaixaEnum;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long>, JpaSpecificationExecutor<Caixa> {

    boolean existsByStatus(StatusCaixaEnum status);

    Optional<Caixa> findByStatus(StatusCaixaEnum status);

    // Bloqueia a linha do caixa até o fim da transação (serializa sangrias e fechamento)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Caixa c where c.id = :id")
    Optional<Caixa> buscarPorIdParaAtualizar(@Param("id") Long id);

    /** Histórico: filtra pela data de abertura e pelo usuário que abriu ou fechou o caixa. */
    static Specification<Caixa> comFiltros(LocalDate inicio, LocalDate fim, String usuario, StatusCaixaEnum status) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (inicio != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dataAbertura"), inicio.atStartOfDay()));
            }
            if (fim != null) {
                filtros.add(cb.lessThan(root.<LocalDateTime>get("dataAbertura"), fim.plusDays(1).atStartOfDay()));
            }
            if (usuario != null && !usuario.isBlank()) {
                filtros.add(cb.or(
                        cb.equal(root.get("usuarioAbertura"), usuario),
                        cb.equal(root.get("usuarioFechamento"), usuario)));
            }
            if (status != null) {
                filtros.add(cb.equal(root.get("status"), status));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }
}
