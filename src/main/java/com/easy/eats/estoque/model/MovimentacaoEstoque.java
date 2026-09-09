package com.easy.eats.estoque.model;

import java.time.LocalDateTime;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.estoque.enums.TipoMovimentacaoEstoque;
import com.easy.eats.produto.model.Produto;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Histórico de entrada/saída de um item de estoque. Cada registro aqui
 * ajusta {@link Estoque#getQuantidadeAtual()} do produto correspondente no
 * momento em que é criado (ver EstoqueService.registrarMovimentacao) — a
 * quantidade em Estoque é sempre a soma de todo o histórico, nunca editada
 * diretamente por fora daqui.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBMOVIMENTACAO_ESTOQUE")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({ "categoria", "empresa" })
    private Produto produto;

    @NotNull(message = "O tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoMovimentacaoEstoque tipo;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidade;

    private String observacao;

    private LocalDateTime dtMovimentacao;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
