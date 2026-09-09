package com.easy.eats.pedidocompra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.fornecedor.model.Fornecedor;
import com.easy.eats.pedidocompra.enums.StatusPedidoCompra;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBPEDIDOCOMPRA")
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O fornecedor é obrigatório")
    @ManyToOne
    @JoinColumn(name = "fornecedor_id", nullable = false)
    @JsonIgnoreProperties({ "empresa" })
    private Fornecedor fornecedor;

    @NotBlank(message = "A descrição dos itens é obrigatória")
    private String itens;

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    private StatusPedidoCompra status;

    private LocalDateTime dtCriacao;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
