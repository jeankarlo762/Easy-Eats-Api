package com.easy.eats.pedido.model;

import java.time.LocalDateTime;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.pedido.enums.StatusPedido;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBPEDIDO")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nomeProduto;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidadeProduto;

    private LocalDateTime dataCriacao;

    /**
     * Marcados em {@link com.easy.eats.pedido.service.PedidoService#iniciarPreparo}
     * e {@link com.easy.eats.pedido.service.PedidoService#marcarComoPronto} — sem
     * isso não havia como medir tempo de preparo nenhum, base do Relatório da
     * Cozinha.
     */
    private LocalDateTime dtInicioPreparo;
    private LocalDateTime dtPronto;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

}
