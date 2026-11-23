# Cardápio digital (QR Code) e integração WhatsApp

## Cardápio digital

O front gera, no **Novo Pedido**, o QR Code do link `/cardapio`. A página é pública (sem login):
o cliente escolhe os produtos, informa nome, WhatsApp, entrega (endereço) ou retirada, forma de pagamento
(e troco, se for dinheiro) e envia.

Rotas públicas (liberadas no `SecurityConfig`):

| Método | Rota | O que faz |
|---|---|---|
| GET | `/publico/cardapio` | Produtos e categorias ativos com preço ativo, formas de pagamento aceitas |
| POST | `/publico/cardapio/pedidos` | Cria a venda (`DELIVERY` ou `RETIRADA`, origem `CARDAPIO_DIGITAL`) e a comanda `ABERTO` |

O pedido entra como qualquer outro: aparece na **Tela Cozinha** (aguardando) e em **Mesas → Retiradas e
delivery em aberto**; a conta mostra telefone, endereço e a forma de pagamento escolhida. O pagamento é
registrado normalmente na conta quando o pedido for pago. Cada IP pode fazer até 5 pedidos aceitos a cada 10 minutos.

Novas colunas em `tb_venda` (criadas pelo Hibernate, todas opcionais): `telefone_cliente`, `endereco_entrega`,
`forma_pagamento_prevista`, `troco_para`, `origem`.

Variáveis de ambiente (opcionais):

| Variável | Padrão | |
|---|---|---|
| `CARDAPIO_NOME_ESTABELECIMENTO` | `Easy Eats` | Nome no topo do cardápio e nas mensagens |
| `CARDAPIO_ACEITA_DELIVERY` | `true` | |
| `CARDAPIO_ACEITA_RETIRADA` | `true` | |

## Integração WhatsApp (`com.easyeats.api.integracao.whatsapp`)

Módulo isolado: o resto do sistema só chama `NotificacaoPedidoWhatsApp.novoPedido(...)`. O envio acontece em
segundo plano, depois do commit; se o provedor falhar, o pedido não é afetado (o erro vai para o log).

Ao receber um pedido do cardápio:
- o **cliente** recebe a confirmação com os itens, total, pagamento e entrega (se `WHATSAPP_NOTIFICAR_CLIENTE=true`);
- a **loja** recebe o aviso de novo pedido (se `WHATSAPP_NUMERO_LOJA` estiver preenchido).

Provedores:
- `evolution` (padrão): [Evolution API](https://doc.evolution-api.com), que conecta um número comum de WhatsApp
  pelo QR Code. Envia `POST {WHATSAPP_API_URL}/message/sendText/{WHATSAPP_INSTANCIA}` com o header `apikey`.
- `http`: genérico. `POST {WHATSAPP_API_URL}` com `{"number": "...", "text": "..."}` e `Authorization: Bearer {token}`.
- `log`: não envia, só registra no log.

| Variável | Padrão | |
|---|---|---|
| `WHATSAPP_HABILITADO` | `false` | Desligado, as mensagens só vão para o log |
| `WHATSAPP_PROVEDOR` | `evolution` | `evolution`, `http` ou `log` |
| `WHATSAPP_API_URL` | | Evolution: URL base do servidor. HTTP: URL completa |
| `WHATSAPP_API_TOKEN` | | Evolution: apikey. HTTP: bearer token |
| `WHATSAPP_INSTANCIA` | | Evolution: nome da instância conectada |
| `WHATSAPP_NUMERO_LOJA` | | Número que recebe o aviso de novo pedido (ex.: 5511999998888) |
| `WHATSAPP_NOTIFICAR_CLIENTE` | `true` | |
| `WHATSAPP_DDI_PADRAO` | `55` | Acrescentado a telefones com 10 ou 11 dígitos |

Para conferir (Administrador/Gerente): `GET /integracoes/whatsapp/status` e
`POST /integracoes/whatsapp/teste` com `{"telefone": "11999998888"}`.

Para trocar de API, crie outra implementação de `WhatsAppProvider` (um `@Component` com `nome()` próprio) e
aponte `WHATSAPP_PROVEDOR` para ela.
