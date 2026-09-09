package com.easy.eats.cupom.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.easy.eats.cupom.enums.TipoDesconto;
import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCUPOM", uniqueConstraints = @UniqueConstraint(columnNames = { "empresa_id", "codigo" }))
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O código do cupom é obrigatório")
    private String codigo;

    @NotNull(message = "O tipo de desconto é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoDesconto tipoDesconto;

    @NotNull(message = "O valor do desconto é obrigatório")
    @Positive(message = "O valor do desconto deve ser maior que zero")
    private BigDecimal valorDesconto;

    private LocalDateTime dtValidadeInicio;
    private LocalDateTime dtValidadeFim;

    // Nulo = sem limite. Os dois juntos cobrem tanto "uso único" (1/1) quanto
    // "uso múltiplo" (limite alto ou nulo por cliente).
    private Integer limiteUsoTotal;
    private Integer limiteUsoPorCliente;

    private BigDecimal valorMinimoPedido;
    private Boolean flAtivo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
