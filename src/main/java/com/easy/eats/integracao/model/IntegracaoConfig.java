package com.easy.eats.integracao.model;

import com.easy.eats.empresa.model.model.Empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Uma linha por (empresa, chave de integração) — ex.: (empresa 3,
 * "mercadopago"). "ativo" é só a marcação de que o admin preencheu e salvou
 * credenciais; não há verificação real de conectividade com o provedor
 * (Mercado Pago, iFood, WhatsApp Business, impressora de rede) — isso exige
 * uma conta de desenvolvedor de verdade em cada um, fora do alcance deste
 * sistema testar aqui.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBINTEGRACAOCONFIG")
public class IntegracaoConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "A chave da integração é obrigatória")
    private String chave;

    @Column(columnDefinition = "text")
    private String credenciaisJson;

    private Boolean ativo;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
