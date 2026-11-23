# Easy Eats: Venda (backend)

Registro do que foi implementado para separar **comanda** (o `Pedido`, que vai para a cozinha) de
**venda** (a conta da mesa: total, pagamento, usuário e fechamento). Complementa o
`DOCUMENTACAO_CAIXA.md`. Branch: `feat/venda-caixa`.

## 1. Conceitos

| Termo | Significado | No código |
|---|---|---|
| Comanda | Lançamento de itens na mesa que segue para a cozinha, com status de preparo. | `Pedido` (nome mantido) |
| Venda | Conta da mesa: agrupa as comandas, recebe pagamentos e libera a mesa ao encerrar. | `Venda` (nova) |
| Mesa ocupada | Existe uma venda `ABERTA` nela. | derivado, não há coluna |

O status de preparo da comanda (`ABERTO`, `EM_PREPARO`, `FINALIZADO`, `CANCELADO`) continua como era.
O que mudou: a mesa **não é mais liberada quando a comanda fica pronta**, e sim quando a venda é
finalizada ou cancelada.

## 2. Decisões aprovadas

- `Pedido` continua com esse nome no backend e nas rotas (`/pedido`). "Comanda" é o termo de negócio.
- A venda abre sozinha na primeira comanda da mesa, sem campo novo na requisição.
- A venda tem tipo: `MESA`, `RETIRADA` (já funciona, sem mesa, identificada pelo nome do cliente) e `DELIVERY` (reservado, responde "em breve").
- Sem cadastro de clientes: a venda guarda só `nomeCliente`.
- O cliente pode pagar antes de a cozinha terminar. A mesa só libera ao finalizar a venda.
- Caixa aberto é obrigatório para finalizar uma venda.
- Cancelar venda é permitido com comandas em preparo. Devolver o estoque é uma opção marcada pelo gerente e confirmada com a senha dele; vale para comandas que baixaram o estoque mas ainda não foram usadas.
- Sem desconto, acréscimo ou taxa de serviço. Junção e troca de mesa ficam para depois.
- Sem migrations: as tabelas novas são criadas pelo Hibernate.

## 3. Modelo de dados

**`Venda`** (`tb_venda`): `mesa` (opcional), `status` (`ABERTA`, `FINALIZADA`, `CANCELADA`),
`mesaAbertaId`, `nomeCliente`, `observacao`, usuário e data de abertura, usuário e data de fechamento,
`valorTotal` (gravado ao finalizar), `caixa` (caixa que recebeu), `motivoCancelamento`, `@Version`.
`mesaAbertaId` guarda o id da mesa enquanto a venda está aberta e fica nulo ao encerrar. A coluna é
`unique`, então o banco garante uma única venda aberta por mesa mesmo com requisições simultâneas
(mesma técnica do `abertoUnico` do Caixa).

**`PagamentoVenda`** (`tb_pagamento_venda`): venda, `forma` (`DINHEIRO`, `PIX`, `CREDITO`, `DEBITO`,
`OUTROS`), `valor`, `valorRecebido` (só dinheiro), usuário e data. A divisão da conta é feita com vários
pagamentos.

**`Venda`** tem `tipo` (`TipoVendaEnum`: `MESA`, `RETIRADA`, `DELIVERY`).

**`Pedido`** ganhou `venda_id` (nulo apenas em pedidos antigos, anteriores à Venda) e `observacao`. A coluna
`mesa_id` passou a aceitar nulo, pois comandas de retirada e delivery não têm mesa (ver seção 11).

**`MovimentacaoCaixa`** ganhou `vendaId` e o tipo `VENDA` (entrada). **`PedidoDto`** ganhou `vendaId`.

## 4. Regras de negócio

1. **Abertura automática.** `POST /pedido` abre a venda da mesa se não houver, ou reaproveita a aberta. A
   linha da mesa é bloqueada, então duas comandas simultâneas não criam duas vendas. O nome do cliente da
   venda vem do campo `cliente` da primeira comanda.
2. **Total da venda** = soma de `quantidade x valorUnitario` dos itens das comandas **não canceladas**,
   calculada por consulta. Não depende de `Pedido.valorTotal` (ver pendências).
3. **Pagamento** (venda aberta): `valor` maior que zero, até 2 casas, e nunca acima do valor restante. Em
   dinheiro, `valorRecebido` (se omitido, igual ao valor) não pode ser menor que o valor, e o troco é a
   diferença. Pix e cartão ignoram `valorRecebido`. Pode ser removido enquanto a venda está aberta, pois
   nenhum dinheiro entra no caixa antes de finalizar.
4. **Finalizar** exige: venda aberta, ao menos uma comanda não cancelada, soma dos pagamentos **igual** ao
   total, e caixa aberto (`422 Abra o caixa antes de finalizar a venda`). Ao finalizar: grava total, caixa,
   usuário e data, libera a mesa e lança no caixa **somente o dinheiro** (soma dos valores em dinheiro, sem
   o troco) como `VENDA`, com `vendaId`. Pix e cartão exigem o caixa aberto mas não geram lançamento.
   A cozinha **não** precisa ter terminado.
5. **Cancelar** (Administrador e Gerente, com motivo): só vendas abertas. Cancela todas as comandas. O
   estoque **não volta por padrão**. Com `devolverEstoque = true` e a `senhaGerente` (a senha do próprio
   usuário autenticado, conferida no servidor), os ingredientes voltam nas comandas que baixaram o estoque
   mas ainda não foram usadas: status `ABERTO` (voltou ao início) ou `EM_PREPARO`. Comandas prontas
   (`FINALIZADO`) nunca devolvem. A devolução usa o mesmo cálculo da baixa. Senha ausente ou incorreta:
   422, e nada é cancelado.6. **Comandas de venda não aberta:**
   - Incluir, alterar e excluir comanda ou item (`/pedido`, `/item-pedido`) só com a venda aberta.
   - Venda cancelada: o status de preparo não muda (422).
   - Venda finalizada: o preparo continua livre (a cozinha pode ainda estar terminando), exceto cancelar.
   - A comanda não troca de mesa depois de lançada.
7. **Mesa** com venda aberta não pode ser excluída (422).
8. **Retirada.** `POST /pedido` com `tipoVenda = RETIRADA` não usa mesa, exige o nome do cliente (`cliente`) e
   abre uma venda `RETIRADA` própria. Informando `vendaId` de uma retirada aberta, a nova comanda entra nela.
   Pagamento, finalização, caixa e cancelamento são os mesmos da venda de mesa. Uma retirada não ocupa mesa.
9. **Delivery** (`tipoVenda = DELIVERY`) responde 422 `O delivery estará disponível em breve`, na comanda e
   na abertura de venda.
10. **Observação da comanda** (`observacao`, até 255 caracteres) é gravada em `Pedido`.
11. O usuário gravado vem sempre do token.

## 5. Perfis

| Operação | Perfil |
|---|---|
| Lançar comanda (abre a venda), pagar, remover pagamento, finalizar, consultar venda, ver ocupação | Qualquer usuário autenticado |
| Cancelar venda, histórico (`GET /vendas`) | Administrador e Gerente |

## 6. Endpoints (`/vendas`)

| Método e rota | Função |
|---|---|
| `POST /vendas` | Abre uma venda (`tipo`, `mesaId`, `nomeCliente`, `observacao`). Sem tipo: `MESA` com mesa, `RETIRADA` sem. 201 `VendaDto`. |
| `GET /vendas` | Histórico paginado (Administrador e Gerente). Filtros: `status`, `mesaId`, `usuario`, `inicio`, `fim` (data de fechamento, ou de abertura se ainda aberta), `pagina`, `tamanho`. |
| `GET /vendas/abertas` | Vendas abertas (mesas e retiradas), da mais antiga para a mais nova, com o total parcial. Qualquer usuário. |
| `GET /vendas/resumo` | Faturamento do período (`inicio`, `fim`; sem datas, hoje): total, quantidade, ticket médio, canceladas, por forma de pagamento e por dia, pela data de fechamento. Administrador e Gerente. |
| `GET /vendas/ocupacao-mesas` | Todas as mesas ativas: `ocupada`, `vendaId`, `nomeCliente`, `quantidadeComandas`, `valorParcial`. |
| `GET /vendas/mesa/{mesaId}/aberta` | Venda aberta da mesa (204 se não houver). |
| `GET /vendas/{id}` | Detalhe com comandas e pagamentos. |
| `PATCH /vendas/{id}` | Atualiza `nomeCliente` e `observacao` (venda aberta). |
| `POST /vendas/{id}/pagamentos` | Registra pagamento (`forma`, `valor`, `valorRecebido`). 201 `VendaDto`. |
| `DELETE /vendas/{id}/pagamentos/{pagamentoId}` | Remove pagamento (venda aberta). |
| `POST /vendas/{id}/finalizar` | Finaliza a venda e libera a mesa. |
| `POST /vendas/{id}/cancelar` | Cancela com `{ "motivo": "...", "devolverEstoque": false, "senhaGerente": "..." }`. |

`VendaDto`: `id`, `tipo`, `mesaId`, `numeroMesa`, `status`, `nomeCliente`, `observacao`, `usuarioAbertura`,
`dataAbertura`, `usuarioFechamento`, `dataFechamento`, `valorTotal`, `valorPago`, `valorRestante`,
`caixaId`, `motivoCancelamento`, `comandas` (lista de `PedidoDto`), `pagamentos`.

Erros seguem o padrão do projeto: 400 validação, 403 sem permissão, 404 não encontrado, 422 regra de
negócio. Mensagens principais: `A venda não está aberta`, `Só é possível cancelar uma venda aberta`,
`O valor pago (R$ x) difere do total da venda (R$ y)`, `O pagamento de R$ x ultrapassa o valor restante da
venda (R$ y)`, `Abra o caixa antes de finalizar a venda`, `A venda não tem comandas para finalizar`.

## 7. Caixa: mudanças relacionadas

- Novo tipo `VENDA` em `TipoMovimentacaoCaixaEnum`, **não lançável** pela API do caixa
  (`As vendas entram no caixa ao finalizar a venda`).
- `CaixaService.registrarVenda` (bloqueia o caixa e lança o dinheiro). Entra no valor esperado do
  fechamento.
- `RelatorioFechamentoDto.totalVendas` (dinheiro de vendas) e `MovimentacaoCaixaDto.vendaId`.

## 8. Arquivos

Novos: `entity/` (`Venda`, `PagamentoVenda`, `StatusVendaEnum`, `FormaPagamentoEnum`), `dto/` (nove DTOs
da venda e `OcupacaoMesaDto`), `dto/mapper/VendaMapper`, `repository/` (`VendaRepository`,
`PagamentoVendaRepository`), `service/VendaService`, `controller/VendaController`.

Alterados: `Pedido`, `PedidoDto`, `PedidoMapper`, `PedidoRepository` (consultas da venda),
`MesaRepository` (lock), `PedidoService`, `ItemPedidoService`, `MesaService`, `EstoqueService`
(`devolverPorItens`), `PedidoController` (recebe o usuário), `CaixaService`, `CaixaMapper`,
`MovimentacaoCaixa`, `MovimentacaoCaixaDto`, `RelatorioFechamentoDto`, `TipoMovimentacaoCaixaEnum` e
`IntegrationTestBase`.

## 9. Compatibilidade com o app mobile

O contrato de `/pedido`, `/mesa` e `/dispositivos` foi preservado: o app continua enviando o mesmo JSON, e a
resposta só ganha `vendaId`. Diferenças de comportamento que o app pode encontrar:

- Mudar o status de uma comanda de **venda cancelada** devolve 422 (o app mostra o erro).
- Cancelar uma comanda de **venda finalizada** devolve 422.
- Alterar ou excluir comanda de venda não aberta devolve 422.
- Lançar comanda em mesa ocupada agora entra na venda aberta (antes não havia o conceito).

## 10. Testes

Total **251 testes, 0 falhas** (153 anteriores + 98 novos), em H2 e também em PostgreSQL 18 (banco
temporário, removido depois). Novos: `VendaComandaTest` (22), `VendaPagamentoFinalizacaoTest` (28),
`VendaCancelamentoTest` (20), `VendaConsultaTest` (7), `VendaRetiradaTest` (16) e `VendaResumoTest` (5).
Cobrem o cenário central (comanda pronta não libera a mesa), pagamentos e troco, finalização com e sem
caixa, entrada de dinheiro no caixa sem o troco, travas das comandas, cancelamento com e sem devolução de
estoque e senha do gerente, retirada, delivery indisponível, vendas abertas, resumo do período, permissões e
filtros.

## 11. Limitações e pendências

- **Bancos criados antes da venda/caixa.** O Hibernate (`ddl-auto=update`) não altera restrições existentes. A migration `V6__ajusta_restricoes_venda_caixa.sql` aceita o tipo `VENDA` em `tb_movimentacao_caixa` e remove o `NOT NULL` de `tb_pedido.mesa_id` (retirada). Ela roda sozinha no PostgreSQL ao subir a API e não faz nada em banco novo.
- **`Pedido.valorTotal`** não é recalculado quando itens são alterados (`PUT /pedido` e `/item-pedido`),
  problema anterior à Venda. O total da venda não sofre, porque é calculado pelos itens.
- **Cancelar uma comanda isolada** continua sem devolver estoque (comportamento anterior, fixado por
  teste). A devolução só ocorre no cancelamento da venda.
- A senha do gerente é a do próprio usuário autenticado (que já precisa ser Administrador ou Gerente).
  Autorização por outro gerente (usuário e senha diferentes) não foi implementada.
- Relatório do caixa por forma de pagamento (Pix, cartão) não existe: só o dinheiro entra no caixa. O resumo
  de vendas (`/vendas/resumo`) já traz os totais por forma de pagamento.
- Concorrência de finalização e pagamentos é protegida por lock pessimista, mas não há teste de carga.

## 12. Frontend da Venda (repositório `Easy-Eats-Frontend`)

| Rota | Perfil | O que faz |
|---|---|---|
| `/mesas` | todos | Ocupação vinda de `GET /vendas/ocupacao-mesas` (a mesa só libera quando a conta é finalizada ou cancelada), atalho **Conta** na mesa ocupada, **Retiradas em aberto** e **Nova retirada**. |
| `/conta/:id` | todos (cancelar: Administrador e Gerente) | Comandas e itens, pagamentos (forma, valor, valor recebido e troco), **Finalizar venda** (total pago igual ao total e caixa aberto, com atalho para abrir o caixa) e **Cancelar venda** com motivo, a opção **Devolver ao estoque os itens que ainda não foram usados** e a senha do gerente. |
| `/novo-pedido` | todos | O botão **Finalizar pedido** abre um modal: atendimento (**Mesa**, **Retirada** ou **Delivery** desabilitado com "Em breve"), mesa, nome do cliente (obrigatório na retirada), **observações** e **forma de pagamento**. Na mesa o pagamento é registrado na conta (a mesa continua ocupada). Na retirada paga na hora o pedido é pago e finalizado (exige caixa aberto); "pagar na retirada" deixa a conta em aberto. |
| `/financeiro` | Administrador e Gerente | Faturamento por período a partir das vendas finalizadas: total, ticket médio, quantidade, canceladas, por forma de pagamento, por dia e lista de vendas com link para a conta. |
| `/dashboard` | Administrador e Gerente | Faturamento do dia e da semana vindo de `GET /vendas/resumo`; indicadores de operação continuam vindo das comandas. |

O modelo da comanda no frontend passou a se chamar `Comanda`/`ComandaService` (`src/app/comandas`), e o da
venda é `Venda`/`VendaService` (`src/app/vendas`). O relatório de fechamento do caixa e a lista de lançamentos
mostram o tipo `VENDA` (dinheiro de vendas) com link para a conta de origem.

Testes do frontend: 108 (eram 56), build de produção sem avisos.