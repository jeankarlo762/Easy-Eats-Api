package com.easy.eats.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easy.eats.empresa.model.model.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa,Integer>{
    
}
