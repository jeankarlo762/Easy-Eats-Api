# Easy Eats: histórico do desenvolvimento do Caixa

Este documento registra o que foi analisado, decidido e implementado até agora na branch
`feat/venda-caixa`. Ele serve de referência para quem for continuar o trabalho (frontend do
Caixa e, depois, a Venda).

- Branch: `feat/venda-caixa` (backend e frontend). Nada foi enviado ao GitHub ainda.
- Repositórios: backend `ZacML/Easy-Eats-Api-2`, frontend `joao-vitor1007/Easy-Eats-Frontend`,
  app mobile `ZacML/Easy-Eats-app` (somente leitura, não foi alterado).

---

## 1. Situação encontrada

### 1.1 Projetos

| Projeto | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Web MVC, Data JPA, Validation, Security (JWT), Actuator, Flyway, springdoc, Lombok. PostgreSQL em desenvolvimento e H2 nos testes. |
| Frontend | Angular 21 zoneless (sem zone.js), componentes standalone, `signal()` e `inject()`, rotas com lazy loading, SCSS. |
| Mobile | Expo / React Native. Usa `/auth/login`, `/mesa`, `/pedido`, `/produto`, `/preco`, `/categorias`, `/estoques/produtos-sem-estoque` e `/dispositivos/push-token`. |

### 1.2 Arquitetura do backend (preservada)

Camadas por tipo de pacote: `controller → service → repository → entity`, mais `dto` (records) com
`dto/mapper` (classes manuais), `config`, `security` e `exception`.

- Erros: `GlobalExceptionHandler` converte `RecursoNaoEncontradoException` (404),
  `RegraNegocioException` (422), `IllegalArgumentException` (422), validação (400) e lock otimista (409)
  em `{ "erro": "...", "campos": [...] }`.
- Convenções: entidades com `@Data @Entity`, datas por `@PrePersist`/`@PreUpdate`, valores monetários
  em `BigDecimal`, lock pessimista para operações concorrentes (como a baixa de estoque).
- Banco híbrido: a migration V4 cria `tb_categoria`, `tb_ingrediente`, `tb_estoques`, `tb_produto`, `tb_preco`, `tb_produto_ingrediente` e `tb_mesa`. As demais (`tb_pedido`, `tb_item_pedido`, `tb_dispositivo_push` e as do Caixa) são criadas pelo Hibernate (`ddl-auto=update`).

### 1.3 Contexto do financeiro antes deste trabalho

- Não existia nenhuma estrutura de caixa, contas a pagar ou lançamento financeiro no backend.
- O faturamento do frontend era calculado no navegador somando pedidos `FINALIZADO` (que significa
  "pronto na cozinha", não "pago").
- A ocupação da mesa também é calculada no frontend: mesa ocupada = existe pedido `ABERTO` ou
  `EM_PREPARO`. Por isso a mesa aparece livre quando o pedido fica pronto.

### 1.4 Problemas conhecidos (fora do escopo desta entrega)

Registrados na análise inicial e **não alterados**: `/usuarios/**` e `/actuator/**` públicos, log de
senha em `UsuarioController`, segredos no `application.properties`, ausência de autorização por perfil
na API (fora das rotas novas do caixa), mistura de Hibernate `update` com Flyway parcial, uso de
`RuntimeException` em alguns services e `GET /pedido` sem paginação.

---

## 2. Preparação do ambiente

- Git configurado globalmente: `user.name = PedroHenriqueAraujoRocha`,
  `user.email = pedro_ha_rocha@estudante.sesisenai.org.br`.
- Clonados em `EasyEats/`: `Easy-Eats-Api-2`, `Easy-Eats-Frontend`, `Easy-Eats-app`.
- As pastas `Easy-Eats-Api-2-master` e `Easy-Eats-Frontend-main` (cópias de ZIP) têm o mesmo conteúdo
  dos clones e **não devem ser editadas**.
- Branch criada nos repositórios de backend e frontend: `feat/venda-caixa`.
- Ambiente de build: JDK 24 na máquina (o projeto compila com release 21), Maven pelo wrapper.
- Linha de base antes das mudanças: 92 testes passando.

---

## 3. Decisões tomadas

| Tema | Decisão |
|---|---|
| Ordem de entrega | Caixa primeiro, Venda depois. |
| Migrations | Não alterar. As tabelas novas do Caixa são criadas pelo Hibernate. |
| Services existentes | Podem ser alterados quando necessário (previsto para `PedidoService` na Venda). Para o Caixa foram criados services novos. |
| Nome `Pedido` | Mantido no backend. "Comanda" é o termo de negócio e de tela. |
| Caixa | Um único caixa aberto por vez. |
| Perfis do Caixa | Administrador e Gerente fazem tudo. Funcionário abre o caixa, vê o caixa atual e lança suprimento. |
| Fechamento cego | A tela não mostra o valor esperado até o operador informar o valor contado. |
| Diferença no fechamento | Justificativa obrigatória quando o valor contado difere do esperado. |
| Entrada avulsa | Fora desta fase. Só fundo inicial, suprimento e sangria. |
| Nome do usuário | O `username` do login (o sistema não tem nome completo). |
| Relatório de fechamento | Tela com botão Imprimir (CSS de impressão), sem biblioteca de PDF. |
| Venda sem mesa | A futura Venda já deve aceitar venda sem mesa (delivery e retirada). |
| Clientes | Sem cadastro de clientes por enquanto. |
| Venda e cozinha | O cliente pode pagar antes de a cozinha terminar. A mesa só libera quando a venda é finalizada. |
| Cancelamento | Cancelar venda em preparo deve devolver o estoque dos itens cujo preparo não começou. |
| Caixa e Venda | O caixa é obrigatório para finalizar uma venda. |
| Fora de escopo | Desconto, acréscimo, taxa de serviço, junção e troca de mesa. |

---

## 4. Backend do Caixa: o que foi implementado

### 4.1 Modelo de dados

**`Caixa`** (`tb_caixa`): turno do caixa.

| Campo | Descrição |
|---|---|
| `status` | `ABERTO` ou `FECHADO`. |
| `abertoUnico` | `true` enquanto aberto, `null` ao fechar. Coluna `unique`: o banco aceita vários `null` e só um `true`, garantindo um único caixa aberto mesmo com aberturas simultâneas. |
| `fundoCaixa` | Valor inicial informado na abertura. |
| `usuarioAbertura`, `dataAbertura`, `observacaoAbertura` | Quem abriu, quando e observação. |
| `usuarioFechamento`, `dataFechamento` | Quem fechou e quando. |
| `valorEsperado` | Saldo calculado pelo sistema no fechamento (fundo + suprimentos - sangrias). |
| `valorContado` | Valor informado pelo operador. |
| `diferenca` | `valorContado - valorEsperado` (positivo = sobra, negativo = falta). |
| `observacaoFechamento` | Justificativa do fechamento. |
| `versao` | `@Version`. |

**`MovimentacaoCaixa`** (`tb_movimentacao_caixa`): cada lançamento. É imutável (sem edição nem
exclusão). Correções são feitas com novo lançamento.

| Campo | Descrição |
|---|---|
| `caixa` | Caixa do lançamento. |
| `tipo` | `FUNDO_INICIAL` (+), `SUPRIMENTO` (+) ou `SANGRIA` (-). |
| `valor`, `descricao`, `usuario`, `dataMovimentacao` | Dados do lançamento. |

Índices: status e data de abertura em `tb_caixa`; caixa e data em `tb_movimentacao_caixa`.
Valores monetários: `DECIMAL(12,2)`.

### 4.2 Regras de negócio

1. Só pode haver um caixa aberto. Abrir outro devolve 422 `Já existe um caixa aberto`.
2. A abertura cria o lançamento `FUNDO_INICIAL` quando o fundo é maior que zero.
3. Suprimento e sangria exigem caixa `ABERTO` e valor maior que zero com até 2 casas decimais.
4. `FUNDO_INICIAL` não pode ser lançado pela API depois da abertura.
5. A sangria exige motivo (`descricao`), não pode ultrapassar o saldo e é restrita a Administrador e Gerente.
6. O fechamento é restrito a Administrador e Gerente. O sistema calcula o valor esperado, grava a
   diferença e libera nova abertura. Diferença diferente de zero exige justificativa.
7. Caixa fechado não aceita lançamentos nem novo fechamento (422 `O caixa já está fechado`).
8. O usuário gravado vem **sempre do token**. `usuario`, `caixaId` e `dataMovimentacao` enviados no
   corpo são ignorados.
9. Sangrias e fechamento usam lock pessimista na linha do caixa, evitando sangria dupla acima do saldo.
10. O saldo é calculado por uma única consulta agregada por tipo. Caixa fechado reutiliza o valor gravado.

### 4.3 Perfis

| Operação | Perfil |
|---|---|
| Abrir caixa, ver caixa atual, lançar suprimento | Qualquer usuário autenticado |
| Sangria, fechar caixa, histórico, detalhe, lançamentos, resumo, relatório | Administrador e Gerente (`ROLE_ADMIN`, `ROLE_GERENTE`) |

A verificação está no `CaixaService`. Sem permissão: 403 com `{ "erro": "Operação permitida apenas para
Administrador ou Gerente" }`. O `SecurityConfig` não foi alterado.

### 4.4 Endpoints (`/caixa`)

| Método e rota | Corpo / parâmetros | Resposta |
|---|---|---|
| `POST /caixa/abrir` | `{ "fundoCaixa": 100.00, "observacao": "..." }` | 201 `CaixaDto` |
| `GET /caixa/atual` | | 200 `CaixaDto` ou 204 sem caixa aberto |
| `POST /caixa/{id}/movimentacoes` | `{ "tipo": "SUPRIMENTO"\|"SANGRIA", "valor": 10.00, "descricao": "..." }` | 201 `MovimentacaoCaixaDto` |
| `POST /caixa/{id}/fechar` | `{ "valorContado": 120.00, "observacao": "..." }` | 200 `CaixaDto` |
| `GET /caixa` | `inicio`, `fim` (yyyy-MM-dd), `usuario`, `status`, `pagina`, `tamanho` | 200 `PaginaDto<CaixaDto>` |
| `GET /caixa/{id}` | | 200 `CaixaDto` |
| `GET /caixa/{id}/relatorio-fechamento` | só para caixa `FECHADO` | 200 `RelatorioFechamentoDto` |
| `GET /caixa/movimentacoes` | `inicio`, `fim`, `tipo`, `usuario`, `caixaId`, `pagina`, `tamanho` | 200 `PaginaDto<MovimentacaoCaixaDto>` |
| `GET /caixa/movimentacoes/resumo` | mesmos filtros (sem página) | 200 `ResumoLancamentosDto` |

Filtros de data: `inicio` e `fim` inclusivos. Paginação: `pagina` começa em 0, `tamanho` padrão 20 e
limite 100. Ordenação fixa: mais recentes primeiro. `usuario` no histórico considera quem abriu ou fechou.

### 4.5 Formatos de resposta

`CaixaDto`:

```json
{
  "id": 1,
  "status": "FECHADO",
  "fundoCaixa": 100.00,
  "usuarioAbertura": "ana",
  "dataAbertura": "2026-03-10T09:00:00",
  "observacaoAbertura": null,
  "usuarioFechamento": "gerente",
  "dataFechamento": "2026-03-10T17:00:00",
  "valorEsperado": 100.00,
  "valorContado": 95.00,
  "diferenca": -5.00,
  "observacaoFechamento": "Falta no troco",
  "saldoAtual": 100.00
}
```

`MovimentacaoCaixaDto`: `id`, `caixaId`, `tipo`, `valor`, `descricao`, `usuario`, `dataMovimentacao`.

`PaginaDto<T>`: `itens`, `total`, `pagina`, `tamanho`, `totalPaginas`.

`ResumoLancamentosDto`: `totalEntradas`, `totalSaidas`, `saldo`.

`RelatorioFechamentoDto`: `caixa` (`CaixaDto`), `totalSuprimentos`, `totalSangrias` e `movimentacoes`
(lista cronológica de `MovimentacaoCaixaDto`).

### 4.6 Erros

| Situação | Status | Exemplo de `erro` |
|---|---|---|
| Dados inválidos | 400 | `Dados inválidos` com `campos: ["valor: O valor deve ser maior que zero"]` |
| Sem permissão | 403 | `Operação permitida apenas para Administrador ou Gerente` |
| Caixa não existe | 404 | `Caixa não encontrado: 999` |
| Regra de negócio | 422 | `Já existe um caixa aberto`, `O caixa já está fechado`, `Informe o motivo da sangria`, `Saldo insuficiente para a sangria: disponível R$ 100.00, solicitado R$ 150.00`, `Informe a justificativa da diferença de R$ 5.00 no fechamento`, `O relatório só está disponível após o fechamento do caixa` |
| Parâmetro de URL com valor inválido (ex.: `tipo=XYZ`) | 422 | tratado pelo handler de `IllegalArgumentException` já existente |

### 4.7 Arquivos criados

Todos em `src/main/java/com/easyeats/api/`:

- `entity/`: `Caixa`, `MovimentacaoCaixa`, `StatusCaixaEnum`, `TipoMovimentacaoCaixaEnum`.
- `dto/`: `AberturaCaixaDto`, `FechamentoCaixaDto`, `CaixaDto`, `MovimentacaoCaixaDto`,
  `RelatorioFechamentoDto`, `ResumoLancamentosDto`, `FiltroLancamentosDto`, `TotalPorTipoDto`, `PaginaDto`.
- `dto/mapper/CaixaMapper`.
- `repository/`: `CaixaRepository`, `MovimentacaoCaixaRepository`, `MovimentacaoCaixaRepositoryCustom` e
  `MovimentacaoCaixaRepositoryImpl` (consulta agregada por tipo).
- `security/UsuarioAutenticado` (usuário e perfil de gestão a partir do token).
- `service/CaixaService` e `controller/CaixaController`.

### 4.8 Arquivos existentes alterados

| Arquivo | Alteração |
|---|---|
| `exception/GlobalExceptionHandler` | Novo handler: `AccessDeniedException` responde 403 com `{ "erro": "..." }`. |
| `test/.../support/IntegrationTestBase` | Limpa as tabelas do caixa entre testes, cria o usuário de teste padrão com perfil ADMIN, e ganhou `tokenDe(username, perfis...)`, `postComo(...)` e `getComo(...)` para testar outros perfis. |

### 4.9 Extensões à arquitetura (pendentes de aval)

1. Filtros dinâmicos com `Specification` (`JpaSpecificationExecutor`), no lugar de `@Query` com
   parâmetros opcionais, que costumam falhar no PostgreSQL com parâmetro nulo tipado.
2. `PaginaDto`, para não serializar o `Page` do Spring diretamente.

---

## 5. Testes

Total: **153 testes, 0 falhas** (92 anteriores + 61 novos). Comando: `.\mvnw.cmd test`.

| Classe | Testes | Cobertura |
|---|---|---|
| `CaixaAberturaFechamentoTest` | 25 | Abertura (valores válidos e inválidos), único caixa aberto (regra e restrição do banco), perfis, caixa atual, fechamento com e sem diferença, justificativa, caixa fechado ou inexistente. |
| `CaixaMovimentacaoTest` | 20 | Suprimento e sangria, saldo, motivo obrigatório, sangria acima do saldo, perfis, validações, campos ignorados, caixa fechado ou inexistente. |
| `CaixaConsultaTest` | 16 | Relatório de fechamento, histórico paginado e filtrado, lançamentos com filtros combinados, resumo, limite de página e permissões. |

Os testes automatizados rodam em H2. A aplicação também foi subida em um PostgreSQL 18 vazio (banco
`easy_eats` criado para isso): as 5 migrations foram aplicadas, o Hibernate criou `tb_caixa` e
`tb_movimentacao_caixa`, e a restrição `unique` de `aberto_unico` foi conferida diretamente no banco
(um caixa aberto aceita vários fechados e recusa um segundo aberto). **A suíte de testes completa ainda
não foi executada contra PostgreSQL.**

---

## 6. Compatibilidade com o app mobile

O contrato de `/pedido`, `/mesa`, `/produto`, `/preco`, `/categorias`, `/estoques` e `/dispositivos`
não foi alterado. A Venda deverá abrir sozinha dentro do `POST /pedido`, sem campo novo, para o app
continuar funcionando. Pontos a tratar na etapa da Venda:

- O app permite voltar o status do pedido (`EM_PREPARO → ABERTO`, `FINALIZADO → EM_PREPARO`). Quando a
  venda estiver finalizada ou cancelada, a API responderá 422 e o app mostrará o erro.
- O tipo `Pedido.mesa` do app é `string`, mas a API devolve `mesaId`. Divergência já existente, sem efeito hoje.

---

## 7. O que falta

### 7.1 Frontend do Caixa (implementado, commit `67ab211` do repositório do frontend)

Telas novas ou reescritas em `Easy-Eats-Frontend`, seguindo o design system global (`styles.scss`), o
modo escuro e signals (o app é zoneless):

| Rota | Perfil | O que faz |
|---|---|---|
| `/caixa` | todos | Abrir caixa, lançar suprimento, sangria (gestão) e fechar caixa (gestão) com fechamento cego. Mostra os lançamentos do turno só para a gestão. Após fechar, exibe o resumo e o link do relatório. |
| `/caixa-historico` | Administrador e Gerente | Histórico de caixas com filtro de período, status e usuário, com paginação no servidor. |
| `/caixa-relatorio/:id` | Administrador e Gerente | Relatório de fechamento com nome dos usuários, totais, diferença, lançamentos e botão Imprimir (CSS de impressão, linhas de assinatura). |
| `/gestao-financeira` (menu "Lançamentos") | Administrador e Gerente | Lançamentos do caixa com filtro de período, tipo, usuário e caixa, totais de entradas, saídas e saldo, e paginação. |

Menu: o item Financeiro virou um grupo com Visão Geral, Caixa, Histórico de Caixas e Lançamentos. O
Funcionário vê só Caixa.

Arquivos principais: `caixa.service.ts` (cliente de `/caixa`), componente reutilizável
`components/paginacao`, regras de impressão em `styles.scss`. O service legado
`movimentacao-financeira.service.ts` (API que nunca existiu) foi removido.

Testes do frontend: 56 (eram 18). Build de produção sem avisos.
### 7.2 Venda e Comanda (backend implementado)

Veja `DOCUMENTACAO_VENDA.md`. O frontend da Venda (renomear o modelo `Venda` para `Comanda`, tela da Conta, ocupação de mesa vinda do backend e faturamento por vendas) ainda não foi feito.

### 7.3 Pendências gerais

- Rodar a suíte de testes contra PostgreSQL (hoje só H2; a subida da aplicação e a restrição de caixa único já foram validadas no PostgreSQL).
- Validar visualmente as telas novas no navegador (os testes cobrem lógica e contrato, não o layout).
- Decidir se `Specification` e `PaginaDto` ficam (item 4.9).
- Enviar as branches ao GitHub e abrir pull requests quando for o momento.
