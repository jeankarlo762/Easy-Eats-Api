package com.easyeats.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Conta de uma mesa (ou, futuramente, de um delivery/retirada): agrupa as comandas (Pedido),
 * recebe os pagamentos e só libera a mesa ao ser finalizada ou cancelada.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_venda", indexes = {
        @Index(name = "idx_venda_status", columnList = "status"),
        @Index(name = "idx_venda_data_abertura", columnList = "data_abertura"),
        @Index(name = "idx_venda_mesa", columnList = "mesa_id")
})
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Opcional: venda sem mesa (delivery, retirada)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVendaEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusVendaEnum status;

    // Preenchido com o id da mesa enquanto a venda está aberta e limpo (null) ao encerrar.
    // A restrição unique ignora null, então o banco garante uma única venda aberta por mesa.
    @Column(name = "mesa_aberta_id", unique = true)
    private Long mesaAbertaId;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    private String observacao;

    @Column(name = "usuario_abertura", nullable = false)
    private String usuarioAbertura;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "usuario_fechamento")
    private String usuarioFechamento;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    // Total gravado ao finalizar. Enquanto a venda está aberta o total é calculado pelas comandas.
    @Column(name = "valor_total", precision = 12, scale = 2)
    private BigDecimal valorTotal;

    // Caixa em que a venda foi recebida (obrigatório para finalizar)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Version
    private Long versao;

    @PrePersist
    public void antesDeCriar() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusVendaEnum.ABERTA;
        }
    }
}
