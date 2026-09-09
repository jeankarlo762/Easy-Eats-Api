package com.easy.eats.cashback.model;

import java.math.BigDecimal;

import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Um registro por empresa — criado sob demanda (get-or-create no service). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCASHBACK_CONFIG")
public class CashbackConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O percentual de acúmulo é obrigatório")
    @PositiveOrZero(message = "O percentual não pode ser negativo")
    private BigDecimal percentualAcumulo;

    private BigDecimal valorMinimoParaAcumular;
    private Boolean flAtivo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
