package com.easyeats.api.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.easyeats.api.entity.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByFlativo(String flativo);

    Optional<Mesa> findByNumero(Integer numero);

    // Bloqueia a linha da mesa até o fim da transação (serializa a abertura da venda da mesa)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = :id")
    Optional<Mesa> buscarPorIdParaAtualizar(@Param("id") Long id);
}
