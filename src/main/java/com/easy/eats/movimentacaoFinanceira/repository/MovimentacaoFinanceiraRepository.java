package com.easy.eats.movimentacaoFinanceira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Integer> {

}
