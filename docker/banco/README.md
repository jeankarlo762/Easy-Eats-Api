# Banco de dados — PostgreSQL 16

Este container entrega o PostgreSQL vazio. Quem cria as tabelas é a API, pelo
**Flyway**, na primeira vez que o backend sobe.

## Onde estão os scripts de criação do banco

Os scripts SQL ficam no backend, em
[`src/main/resources/db/migration`](../../src/main/resources/db/migration):

| Script | O que faz |
|---|---|
| `V1__baseline.sql` | Estrutura inicial: todas as tabelas principais (26 `CREATE TABLE`) |
| `V2__unique_email_usuario.sql` | E-mail de usuário único |
| `V3__valores_monetarios_bigdecimal.sql` | Valores monetários em `numeric` |
| `V4__estoque_unique_produto.sql` | Um registro de estoque por produto |
| `V5__indices_empresa_id.sql` | Índices por empresa |
| `V6__venda_cliente.sql` | Vínculo entre venda e cliente |
| `V7__cliente_cpf.sql` | CPF do cliente |
| `V8__movimentacao_estoque.sql` | Movimentação de estoque |
| `V9__despesa.sql` | Despesas |
| `V10__pedido_tempos_preparo.sql` | Tempos de preparo do pedido |
| `V11__ficha_tecnica.sql` | Ficha técnica dos produtos |
| `V12__pedido_compra.sql` | Pedidos de compra |
| `V13__delivery.sql` | Delivery |
| `V14__notificacao.sql` | Notificações |
| `V15__empresa_endereco_horario.sql` | Endereço e horário da empresa |
| `V16__integracao_config.sql` | Configuração de integrações |
| `V17__remove_stripe.sql` | Remove a integração antiga com Stripe |

O Flyway aplica os scripts em ordem e registra cada um na tabela
`flyway_schema_history`. Por isso eles **não** são montados em
`/docker-entrypoint-initdb.d`: se o Postgres criasse as tabelas antes, o Flyway
tentaria criá-las de novo e a API não subiria.

## O que fica nesta pasta

```
banco/
├── Dockerfile          postgres:16-alpine com fuso America/Sao_Paulo
└── init/
    └── 01-timezone.sh  ajusta o timezone do banco (roda só com o volume vazio)
```

Os dados ficam no volume `banco-dados`. `docker compose down` mantém os dados;
`docker compose down -v` apaga o volume e o banco é recriado do zero na próxima
subida.

## Conferir as tabelas criadas

```sh
docker compose exec banco psql -U postgres -d easyeats -c "\dt"
docker compose exec banco psql -U postgres -d easyeats -c "select version, description from flyway_schema_history order by installed_rank"
```
