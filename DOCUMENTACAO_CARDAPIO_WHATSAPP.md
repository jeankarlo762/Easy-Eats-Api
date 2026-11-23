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
segundo plano, depois do commit; se o WhatsApp falhar, o pedido não é afetado (o erro vai para o log).

O envio usa **Baileys**: o número comum de WhatsApp da loja é conectado pelo QR Code (como no WhatsApp Web) e é
ele que manda a confirmação para o cliente que fez o pedido.

```
Cardápio (cliente) ──> API Easy Eats ──(x-api-key)──> whatsapp-gateway (Node + Baileys) ──> WhatsApp
                          ▲
Tela "WhatsApp" (gestão) ─┘  mostra o QR Code e o status da conexão
```

### Gateway (`whatsapp-gateway/`)

Serviço Node separado (Dockerfile próprio). No Railway: serviço com *root directory* `whatsapp-gateway` e um
volume em `/data` (guarda a sessão, para o número continuar conectado após um deploy).

| Variável | |
|---|---|
| `API_KEY` | Chave compartilhada com a API (mín. 16 caracteres) |
| `AUTH_DIR` | Pasta da sessão (padrão no Docker: `/data/auth`) |
| `PORT` | Porta HTTP (padrão 3000) |

Rotas: `GET /health` (livre), `GET /status`, `POST /send` `{number, text}`, `POST /logout` (header `x-api-key`).

### API

| Variável | Padrão | |
|---|---|---|
| `WHATSAPP_HABILITADO` | `false` | Desligado, as mensagens só vão para o log |
| `WHATSAPP_PROVEDOR` | `baileys` | `baileys`, `http` (genérico) ou `log` |
| `WHATSAPP_API_URL` | | URL do gateway (no Railway, pela rede interna: `http://<servico>.railway.internal:3000`) |
| `WHATSAPP_API_TOKEN` | | A mesma `API_KEY` do gateway |
| `WHATSAPP_NOTIFICAR_CLIENTE` | `true` | Confirmação para o cliente |
| `WHATSAPP_NUMERO_LOJA` | | Opcional: número que também recebe o aviso de novo pedido |
| `WHATSAPP_DDI_PADRAO` | `55` | Acrescentado a telefones com 10 ou 11 dígitos |

Rotas (Administrador/Gerente): `GET /integracoes/whatsapp/conexao` (status + QR), `POST /integracoes/whatsapp/desconectar`,
`GET /integracoes/whatsapp/status` (configuração) e `POST /integracoes/whatsapp/teste` `{"telefone": "11999998888"}`.

### Conectar o número

No sistema, menu **WhatsApp**: aparece o QR Code; no celular da loja, WhatsApp > Dispositivos conectados >
Conectar um dispositivo. Depois de conectado, dá para mandar uma mensagem de teste pela mesma tela.

Observação: Baileys não é a API oficial do WhatsApp. Use um número da loja e evite mensagens em massa, para não
correr risco de bloqueio do número.
