package com.easy.eats.caixa.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.easy.eats.caixa.enums.StatusCaixa;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.usuario.model.Usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sessão de caixa (abertura/fechamento). Pagamentos e movimentações
 * (sangria/suprimento) feitos enquanto uma sessão está ABERTA se vinculam a
 * ela para reconciliação no fechamento.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCAIXA")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private StatusCaixa status;

    @NotNull(message = "O valor inicial é obrigatório")
    private BigDecimal valorInicial;

    private BigDecimal valorApuradoInformado;
    private BigDecimal valorApuradoSistema;
    private BigDecimal diferenca;

    private LocalDateTime dtAbertura;
    private LocalDateTime dtFechamento;

    private String observacoesAbertura;
    private String observacoesFechamento;

    @ManyToOne
    @JoinColumn(name = "usuario_abertura_id")
    private Usuario usuarioAbertura;

    @ManyToOne
    @JoinColumn(name = "usuario_fechamento_id")
    private Usuario usuarioFechamento;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
