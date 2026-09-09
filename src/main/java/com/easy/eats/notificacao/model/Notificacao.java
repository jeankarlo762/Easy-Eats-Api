package com.easy.eats.notificacao.model;

import java.time.LocalDateTime;

import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Empresa nula = notificação de plataforma, visível só para SUPERADMIN (ex.:
 * nova empresa cadastrada). Empresa preenchida = notificação operacional de
 * uma empresa cliente (novo pedido, estoque crítico etc.), visível para
 * quem estiver logado nela.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBNOTIFICACAO")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String icone;
    private String cor;
    private String titulo;
    private String descricao;
    private Boolean lida;
    private LocalDateTime dtCriacao;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
