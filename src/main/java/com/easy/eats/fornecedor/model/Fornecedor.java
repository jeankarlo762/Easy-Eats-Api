package com.easy.eats.fornecedor.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TBFORNECEDOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String cnpj;
    private String telefone;
    private String email;

    private Boolean flAtivo;

    private LocalDateTime dtCriacao;
}