package com.easy.eats.usuario.model;

import com.easy.eats.empresa.model.model.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mesma razão da Empresa: Usuario chega ao Jackson como proxy do Hibernate
// quando vem de getReferenceById (comanda, caixa), e os acessórios do proxy
// quebrariam a serialização da resposta.
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "TBUSUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    // Sem @NotBlank: na criação a ausência é validada manualmente no
    // service (senha é obrigatória); na atualização o campo é opcional —
    // se vier em branco, a senha atual é mantida em vez de forçar reenvio.
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean flAtivo;

    // Usuário técnico criado automaticamente por empresa para representar
    // pedidos originados do link público (cardápio digital), onde não há um
    // funcionário autenticado para preencher Venda.usuario. Não aparece nas
    // listagens de usuário (UsuarioService.listarTodos filtra por este campo).
    private Boolean flSistema;
}