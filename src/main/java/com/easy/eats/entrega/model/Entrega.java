package com.easy.eats.entrega.model;

import java.time.LocalDateTime;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.entrega.enums.StatusEntrega;
import com.easy.eats.entregador.model.Entregador;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "TBENTREGA")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String nomeCliente;

    @NotBlank(message = "O endereço de entrega é obrigatório")
    private String endereco;

    @ManyToOne
    @JoinColumn(name = "entregador_id")
    @JsonIgnoreProperties({ "empresa" })
    private Entregador entregador;

    @Enumerated(EnumType.STRING)
    private StatusEntrega status;

    private LocalDateTime dtCriacao;
    private LocalDateTime dtSaida;
    private LocalDateTime dtEntrega;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
