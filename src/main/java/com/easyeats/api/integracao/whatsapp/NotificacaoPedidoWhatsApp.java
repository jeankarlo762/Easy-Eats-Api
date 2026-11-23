package com.easyeats.api.integracao.whatsapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.easyeats.api.entity.FormaPagamentoEnum;
import com.easyeats.api.entity.ItemPedido;
import com.easyeats.api.entity.Pedido;
import com.easyeats.api.entity.TipoVendaEnum;
import com.easyeats.api.entity.Venda;

import lombok.RequiredArgsConstructor;

/**
 * Mensagens de WhatsApp de um novo pedido do cardápio digital: confirmação para o cliente e aviso para a loja.
 * O restante do sistema só chama {@link #novoPedido}; o envio e o provedor ficam neste módulo.
 */
@Component
@RequiredArgsConstructor
public class NotificacaoPedidoWhatsApp {

    private final WhatsAppService whatsApp;
    private final WhatsAppProperties props;

    public void novoPedido(Venda venda, Pedido pedido, String nomeEstabelecimento) {
        String resumo = resumo(venda, pedido);
        if (props.isNotificarCliente() && venda.getTelefoneCliente() != null) {
            whatsApp.enviar(venda.getTelefoneCliente(),
                    "Olá, " + primeiroNome(venda.getNomeCliente()) + "! Recebemos seu pedido no *"
                            + nomeEstabelecimento + "* 🍔\n\n" + resumo
                            + "\n\nAvisaremos por aqui quando ele sair. Obrigado!");
        }
        if (props.getNumeroLoja() != null && !props.getNumeroLoja().isBlank()) {
            whatsApp.enviar(props.getNumeroLoja(),
                    "🔔 *Novo pedido pelo cardápio digital*\n\nCliente: " + venda.getNomeCliente()
                            + "\nTelefone: " + venda.getTelefoneCliente() + "\n\n" + resumo);
        }
    }

    private String resumo(Venda venda, Pedido pedido) {
        StringBuilder texto = new StringBuilder("*Pedido #").append(pedido.getId()).append("*\n");
        List<ItemPedido> itens = pedido.getItens();
        for (ItemPedido item : itens) {
            texto.append(item.getQuantidade()).append("x ").append(item.getProduto().getNome())
                    .append(" — ").append(moeda(item.getValorUnitario()
                            .multiply(BigDecimal.valueOf(item.getQuantidade()))))
                    .append('\n');
        }
        texto.append("\n*Total: ").append(moeda(pedido.getValorTotal())).append("*\n");
        texto.append("Pagamento: ").append(forma(venda.getFormaPagamentoPrevista()));
        if (venda.getTrocoPara() != null) {
            texto.append(" (troco para ").append(moeda(venda.getTrocoPara())).append(')');
        }
        texto.append('\n');
        if (venda.getTipo() == TipoVendaEnum.DELIVERY) {
            texto.append("Entrega: ").append(venda.getEnderecoEntrega());
        } else {
            texto.append("Retirada no balcão");
        }
        if (pedido.getObservacao() != null) {
            texto.append("\nObs.: ").append(pedido.getObservacao());
        }
        return texto.toString();
    }

    private static String forma(FormaPagamentoEnum forma) {
        if (forma == null) {
            return "a combinar";
        }
        return switch (forma) {
            case DINHEIRO -> "Dinheiro";
            case PIX -> "Pix";
            case CREDITO -> "Cartão de crédito";
            case DEBITO -> "Cartão de débito";
            case OUTROS -> "Outros";
        };
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
    }

    private static String primeiroNome(String nome) {
        return nome == null ? "" : nome.trim().split("\\s+")[0];
    }
}
