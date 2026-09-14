package com.easy.eats.empresa.model.model;

import java.time.LocalDateTime;

import com.easy.eats.segmento.model.Segmento;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Empresa é atribuída via {@code getReferenceById} em vários services (venda,
 * pedido, comanda, caixa, produto...), então frequentemente chega ao Jackson
 * como proxy do Hibernate. Sem ignorar os acessórios do proxy, a serialização
 * tenta escrever {@code hibernateLazyInitializer}/{@code handler} e quebra a
 * resposta.
 */
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "TBEMPRESA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome da empresa é obrigatório")
    private String nome;

    @NotBlank(message = "O CNPJ é obrigatório")
    private String cnpj;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String telefone;
    private String endereco;
    private String horarioFuncionamento;
    private Boolean flAtivo;
    private LocalDateTime dtCriacao;
    private LocalDateTime dtAlteracao;

    // Identificador usado no link público do cardápio (ex.: /cardapio/pizzaria-do-joao).
    // Gerado automaticamente a partir do nome e editável pelo administrador da empresa.
    private String slug;

    @ManyToOne
    @JoinColumn(name = "segmento_id")
    private Segmento segmento;

}