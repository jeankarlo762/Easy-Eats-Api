package com.easy.eats.empresa.model.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBEMPRESA")
@Getter
@Setter
@Builder
public class Empresa {

    private Integer id;
    private String nome;
    private String cnpj;
    private String email;
    private String telefone;
    private Boolean flAtivo;
    private LocalDateTime dtCriacao;
    private LocalDateTime dtAlteracao;

}