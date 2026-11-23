package com.easyeats.api.repository;

import com.easyeats.api.dto.FiltroVendaDto;
import com.easyeats.api.dto.TotalDiaDto;
import com.easyeats.api.dto.TotalPeriodoDto;
import com.easyeats.api.entity.StatusVendaEnum;
import com.easyeats.api.entity.Venda;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {

    @EntityGraph(attributePaths = "mesa")
    Optional<Venda> findByMesaIdAndStatus(Long mesaId, StatusVendaEnum status);

    boolean existsByMesaIdAndStatus(Long mesaId, StatusVendaEnum status);

    // Mesa carregada junto (evita 1 consulta por venda ao montar a ocupação das mesas)
    @EntityGraph(attributePaths = "mesa")
    List<Venda> findByStatus(StatusVendaEnum status);

    // Vendas abertas em ordem de abertura (mesas e retiradas), com a mesa carregada junto
    @EntityGraph(attributePaths = "mesa")
    List<Venda> findByStatusOrderByDataAberturaAscIdAsc(StatusVendaEnum status);

    // Faturamento do período: vendas do status informado, pela data de fechamento
    @Query("select new com.easyeats.api.dto.TotalPeriodoDto(count(v), sum(v.valorTotal)) from Venda v "
            + "where v.status = :status and v.dataFechamento >= :de and v.dataFechamento < :ate")
    TotalPeriodoDto totalizarPeriodo(@Param("status") StatusVendaEnum status,
            @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);

    @Query("select new com.easyeats.api.dto.TotalDiaDto(cast(v.dataFechamento as LocalDate), count(v), "
            + "sum(v.valorTotal)) from Venda v "
            + "where v.status = :status and v.dataFechamento >= :de and v.dataFechamento < :ate "
            + "group by cast(v.dataFechamento as LocalDate) order by cast(v.dataFechamento as LocalDate)")
    List<TotalDiaDto> totalizarPorDia(@Param("status") StatusVendaEnum status,
            @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);

    // Bloqueia a linha da venda até o fim da transação (serializa pagamentos, finalização e cancelamento)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venda v where v.id = :id")
    Optional<Venda> buscarPorIdParaAtualizar(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = "mesa")
    Page<Venda> findAll(Specification<Venda> spec, Pageable pageable);

    static Specification<Venda> comFiltros(FiltroVendaDto filtro) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (filtro.status() != null) {
                filtros.add(cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.mesaId() != null) {
                filtros.add(cb.equal(root.get("mesa").get("id"), filtro.mesaId()));
            }
            if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
                filtros.add(cb.or(
                        cb.equal(root.get("usuarioAbertura"), filtro.usuario()),
                        cb.equal(root.get("usuarioFechamento"), filtro.usuario())));
            }
            // data de referência: fechamento (finalizada ou cancelada) ou, se ainda aberta, abertura
            Expression<LocalDateTime> referencia = cb.coalesce(
                    root.<LocalDateTime>get("dataFechamento"), root.<LocalDateTime>get("dataAbertura"));
            if (filtro.inicio() != null) {
                filtros.add(cb.greaterThanOrEqualTo(referencia, filtro.inicio().atStartOfDay()));
            }
            if (filtro.fim() != null) {
                filtros.add(cb.lessThan(referencia, filtro.fim().plusDays(1).atStartOfDay()));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }
}