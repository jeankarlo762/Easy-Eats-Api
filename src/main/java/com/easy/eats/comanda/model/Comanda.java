package com.easy.eats.comanda.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

import com.easy.eats.comanda.enums.StatusComanda;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.usuario.model.Usuario;
import com.easy.eats.venda.model.Venda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Guarda-chuva de uma visita à mesa: agrupa uma ou mais {@link Venda}
 * (cada rodada de pedido continua criando uma Venda normalmente, mantendo o
 * fluxo de cozinha existente intacto) até o fechamento, quando o total de
 * todas as vendas vinculadas é cobrado em um único pagamento.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCOMANDA", uniqueConstraints = @UniqueConstraint(columnNames = { "empresa_id", "numero" }))
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O número da comanda é obrigatório")
    private Integer numero;

    @Enumerated(EnumType.STRING)
    private StatusComanda status;

    private String nomeCliente;
    private BigDecimal valorTotal;
    private LocalDateTime dtAbertura;
    private LocalDateTime dtFechamento;

    @JsonIgnoreProperties("vendas")
    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "usuario_abertura_id")
    private Usuario usuarioAbertura;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @JsonIgnoreProperties("comanda")
    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL)
    private List<Venda> vendas;
}
