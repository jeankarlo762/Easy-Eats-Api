package com.easy.eats.usuario.model;

import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TBUSUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String nome;

    private String email;

    private String senha;

    private Boolean flAtivo;
}