package com.easyeats.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_caixa", indexes = {
        @Index(name = "idx_caixa_status", columnList = "status"),
        @Index(name = "idx_caixa_data_abertura", columnList = "data_abertura")
})
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusCaixaEnum status;

    // Preenchido com true apenas enquanto o caixa está aberto e limpo (null) ao fechar.
    // A restrição unique ignora valores null, então o banco garante no máximo um caixa aberto
    // mesmo com duas aberturas simultâneas.
    @Column(name = "aberto_unico", unique = true)
    private Boolean abertoUnico;

    @Column(name = "fundo_caixa", nullable = false, precision = 12, scale = 2)
    private BigDecimal fundoCaixa;

    @Column(name = "usuario_abertura", nullable = false)
    private String usuarioAbertura;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "observacao_abertura")
    private String observacaoAbertura;

    @Column(name = "usuario_fechamento")
    private String usuarioFechamento;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    // Saldo calculado pelo sistema no momento do fechamento (fundo + suprimentos - sangrias)
    @Column(name = "valor_esperado", precision = 12, scale = 2)
    private BigDecimal valorEsperado;

    @Column(name = "valor_contado", precision = 12, scale = 2)
    private BigDecimal valorContado;

    // valorContado - valorEsperado: positivo = sobra, negativo = falta
    @Column(precision = 12, scale = 2)
    private BigDecimal diferenca;

    @Column(name = "observacao_fechamento")
    private String observacaoFechamento;

    @Version
    private Long versao;

    @PrePersist
    public void antesDeCriar() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusCaixaEnum.ABERTO;
        }
    }
}
