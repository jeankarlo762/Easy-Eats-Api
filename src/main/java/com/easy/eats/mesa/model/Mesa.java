package com.easy.eats.mesa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TBMESA")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer numero;
    private String status;
    private String dt_alteracao;
    
    public Mesa(Integer id, Integer numero, String status, String dt_alteracao) {
        this.id = id;
        this.numero = numero;
        this.status = status;
        this.dt_alteracao = dt_alteracao;
    }


}
