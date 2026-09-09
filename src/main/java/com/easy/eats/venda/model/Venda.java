package com.easy.eats.venda.model;

import java.math.BigDecimal;

import java.util.List;

import com.easy.eats.cliente.model.Cliente;
import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TBVENDA")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O status da venda é obrigatório")
    private String status;

    @NotBlank(message = "O tipo da venda é obrigatório")
    private String tipo;

    private String origem;
    private BigDecimal valor_total;
    private BigDecimal desconto;
    private String dt_fechamento;
    private String dt_criacao;
    private String dt_alteracao;

    @JsonIgnoreProperties("venda")
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    private List<ItemVenda> itens;

    @JsonIgnoreProperties("venda")
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos;

    /**
     * Opcional: pedido de mesa (atendimento no salão). Pedidos de balcão/retirada
     * não têm mesa — nesse caso o cliente é identificado por {@link #nomeCliente}.
     * Pelo menos um dos dois deve estar preenchido (validado no service).
     */
    @JsonIgnoreProperties("vendas")
    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    /**
     * Nome do cliente para pedidos sem mesa (balcão/retirada/delivery), ou
     * quando o cliente atendido não é (ainda) um {@link Cliente} cadastrado.
     */
    private String nomeCliente;

    /**
     * Opcional: vincula a venda a um Cliente cadastrado, base para o
     * histórico de compras e para cupom/cashback saberem de quem descontar
     * ou creditar. Sem isso, esses dois módulos ficavam sem qualquer forma de
     * identificar o cliente por trás de uma venda — só o texto livre
     * nomeCliente, que não é uma chave estável (mesmo nome, clientes
     * diferentes; ou o mesmo cliente digitado de formas diferentes).
     */
    @JsonIgnoreProperties({ "enderecos", "empresa" })
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Opcional: comanda à qual esta rodada de pedido pertence (módulo de
     * Comandas). Pedidos de balcão/retirada continuam sem comanda.
     */
    @JsonIgnoreProperties({ "vendas", "mesa" })
    @ManyToOne
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "Usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

}
