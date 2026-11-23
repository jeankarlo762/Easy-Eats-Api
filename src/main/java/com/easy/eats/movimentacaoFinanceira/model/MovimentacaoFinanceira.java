package com.easy.eats.movimentacaoFinanceira.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.movimentacaoFinanceira.enums.TipoMovimentacao;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBMOVIMENTACAO_FINANCEIRA")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    @NotBlank(message = "A categoria é obrigatória")
    private String categoria;

    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    private String descricao;
    private String dt_alteracao;

    // Data real do lançamento (dt_alteracao nunca era preenchido), base do
    // filtro por período do módulo Financeiro.
    private LocalDateTime dataMovimentacao;

    @PrePersist
    private void aoCriar() {
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDateTime.now();
        }
    }

    // Preenchido só em lançamentos de sangria/suprimento feitos durante uma
    // sessão de caixa aberta (módulo de Frente de Caixa). Lançamentos
    // genéricos de fluxo de caixa continuam sem vínculo, como antes.
    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
