package com.easy.eats.notificacao.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.notificacao.model.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findAllByEmpresaIdOrderByDtCriacaoDesc(Integer empresaId, Pageable pageable);

    List<Notificacao> findAllByEmpresaIdIsNullOrderByDtCriacaoDesc(Pageable pageable);

    List<Notificacao> findAllByEmpresaId(Integer empresaId);

    List<Notificacao> findAllByEmpresaIdIsNull();
}
