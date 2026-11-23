package com.easyeats.api.repository;

import com.easyeats.api.dto.TotalFormaPagamentoDto;
import com.easyeats.api.entity.PagamentoVenda;
import com.easyeats.api.entity.StatusVendaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda, Long> {

    List<PagamentoVenda> findByVendaIdOrderByIdAsc(Long vendaId);

    // Total recebido por forma de pagamento nas vendas do status informado, pela data de fechamento
    @Query("select new com.easyeats.api.dto.TotalFormaPagamentoDto(p.forma, sum(p.valor)) from PagamentoVenda p "
            + "where p.venda.status = :status and p.venda.dataFechamento >= :de and p.venda.dataFechamento < :ate "
            + "group by p.forma order by p.forma")
    List<TotalFormaPagamentoDto> totalizarPorForma(@Param("status") StatusVendaEnum status,
            @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);
}