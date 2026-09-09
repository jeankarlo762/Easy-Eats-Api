package com.easy.eats.entregador.model;

import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBENTREGADOR")
public class Entregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do entregador é obrigatório")
    private String nome;

    private String telefone;

    private Boolean flAtivo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
