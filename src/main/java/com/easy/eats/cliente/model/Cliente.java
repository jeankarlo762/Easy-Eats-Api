package com.easy.eats.cliente.model;

import com.easy.eats.endereco.model.Endereco;
import com.easy.eats.pedido.model.Pedido;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private String dt_criacao;
    private String dt_alteracao;

    @OneToMany(mappedBy = "cliente")
    private java.util.List<Pedido> pedidos;

    @OneToMany(mappedBy = "cliente")
    private java.util.List<Endereco> enderecos;
}
