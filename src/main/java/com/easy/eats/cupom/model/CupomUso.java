package com.easy.eats.cupom.model;

import java.time.LocalDateTime;

import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.venda.model.Venda;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Um registro por aplicação bem-sucedida de cupom numa venda — é o que
 * permite checar {@link Cupom#getLimiteUsoTotal()} e
 * {@link Cupom#getLimiteUsoPorCliente()} de verdade (antes, nada contava o
 * uso de um cupom em lugar nenhum do sistema).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBCUPOM_USO")
public class CupomUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cupom_id", nullable = false)
    private Cupom cupom;

    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    // Nulo quando a venda não tem cliente cadastrado vinculado — nesse caso o
    // cupom só entra na contagem do limite total, não no limite por cliente.
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private LocalDateTime dtUso;

    @PrePersist
    private void aoCriar() {
        if (dtUso == null) {
            dtUso = LocalDateTime.now();
        }
    }
}
