package com.easy.eats.fornecedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.fornecedor.model.Fornecedor;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {

}