package com.easy.eats.cliente.model;

import java.math.BigDecimal;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.endereco.model.Endereco;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    private String cpf;

    private String endereco;
    private String dt_criacao;
    private String dt_alteracao;

    // Saldo acumulado de cashback (módulo de Cupons/Cashback). O crédito e o
    // débito de saldo entram junto com o checkout do link público — aqui só o
    // campo é criado, ainda sem lógica de acúmulo automático.
    private BigDecimal saldoCashback;

    @OneToMany(mappedBy = "cliente")
    @JsonIgnoreProperties("cliente")
    private java.util.List<Endereco> enderecos = new java.util.ArrayList<>();
}
