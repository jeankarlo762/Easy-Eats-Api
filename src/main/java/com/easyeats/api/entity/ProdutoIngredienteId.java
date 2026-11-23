package com.easyeats.api.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoIngredienteId implements Serializable {

    private Long produtoId;

    private Long ingredienteId;
}