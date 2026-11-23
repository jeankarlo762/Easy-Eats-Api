package com.easyeats.api.repository;

import com.easyeats.api.dto.TotalPorTipoDto;
import com.easyeats.api.entity.MovimentacaoCaixa;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface MovimentacaoCaixaRepositoryCustom {

    /** Soma os valores por tipo em uma única consulta agregada, respeitando o filtro informado. */
    List<TotalPorTipoDto> totaisPorTipo(Specification<MovimentacaoCaixa> filtro);
}
