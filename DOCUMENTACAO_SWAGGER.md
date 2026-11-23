# Easy Eats: documentação do Swagger

Este documento registra como a API foi documentada no Swagger (OpenAPI) para a apresentação, o que aparece
na tela, o que foi escondido de propósito e como validar. Branch: `feat/swagger-controllers`, criada a partir
da `feat/venda-caixa`.

- Tela do Swagger: `http://localhost:8080/swagger-ui.html`
- Documento OpenAPI (JSON): `http://localhost:8080/v3/api-docs`
- Escopo: somente anotações de documentação. Nenhuma regra de negócio, rota, segurança, migration ou
  contrato de requisição foi alterado.

---

## 1. Como usar na apresentação

1. Em **Autenticação**, execute `POST /auth/login` com usuário e senha. A resposta é o token JWT, em texto.
2. Clique em **Authorize**, no topo da página, cole o token e confirme.
3. As demais rotas passam a enviar o token. Todas as rotas, menos o login, exigem estar autenticado.

Fluxo sugerido para demonstrar, na ordem das seções da tela:

1. **Caixa**: `POST /caixa/abrir` com o fundo inicial.
2. **Mesas**: `GET /mesa` para escolher a mesa.
3. **Pedidos (comandas)**: `POST /pedido` lança a comanda. A venda da mesa abre sozinha.
4. **Pedidos (comandas)**: `PATCH /pedido/{id}/status/EM_PREPARO` baixa o estoque pela ficha técnica.
5. **Vendas**: `GET /vendas/ocupacao-mesas` mostra a mesa ocupada.
6. **Vendas**: `POST /vendas/{id}/pagamentos` registra o pagamento e `POST /vendas/{id}/finalizar` encerra a venda.
7. **Caixa**: `GET /caixa/atual` mostra o saldo com a venda em dinheiro, e `POST /caixa/{id}/fechar` fecha o caixa.

---

## 2. O que aparece e o que fica escondido

| Seção na tela | Controller | Rotas |
|---|---|---|
| Autenticação | `AuthController` | 1 |
| Mesas | `MesaController` | 5 |
| Categorias | `CategoriaController` | 5 |
| Produtos | `ProdutoController` (já estava documentado) | 5 |
| Ingredientes | `IngredienteController` | 6 |
| Ficha técnica | `ProdutoIngredienteController` | 4 |
| Preços | `PrecoController` | 6 |
| Estoque | `EstoqueController` | 10 |
| Pedidos (comandas) | `PedidoController` | 8 (ver 5.1) |
| Itens do pedido | `ItemPedidoController` | 5 |
| Caixa | `CaixaController` | 9 |
| Vendas | `VendaController` | 12 |

Escondidos com `@Hidden`. As rotas continuam funcionando, só não aparecem na tela nem no JSON:

| Controller | Rota | Motivo |
|---|---|---|
| `UsuarioController` | `/usuarios` | Cadastro de usuários e perfis, fora da demonstração. |
| `TesteSecurityController` | `/sec` | Rotas de teste de permissão, não fazem parte do produto. |
| `DispositivoPushController` | `/dispositivos` | Infraestrutura do app mobile. |

---

## 3. O que foi anotado

| Onde | Anotação | Para quê |
|---|---|---|
| Controller | `@Tag`, `@SecurityRequirement(name = "bearerAuth")` | Nome e descrição da seção. O login não exige token. |
| Método | `@Operation`, `@ApiResponse` | Resumo, descrição com as regras reais e as respostas possíveis (200, 201, 204, 400, 403, 404, 409, 422). |
| Parâmetros | `@Parameter` | Filtros (status, mesa, usuário, período), paginação e o status da comanda, com exemplos. |
| DTO | `@Schema` | Descrição e exemplo preenchido por campo. Campos que a API define sozinha são `READ_ONLY`. |

Textos em português. Os padrões seguem o `ProdutoController` e o `ProdutoDto`, que já estavam documentados.

Ajustes em `SwaggerConfig`:
- Descrição da API com o passo a passo de uso e o fluxo do atendimento.
- `OpenApiCustomizer` que junta seções repetidas e as ordena pelo fluxo do sistema (a lista está em
  `ORDEM_DAS_SECOES`). Se uma seção nova for criada, ela aparece no fim até ser incluída nessa lista.

Segurança: não foi alterada. As rotas `/swagger-ui/**` e `/v3/api-docs/**` já eram públicas no `SecurityConfig`.

---

## 4. Como cada resposta foi definida

As respostas documentadas foram conferidas no código (controllers, services e `GlobalExceptionHandler`):

| Código | Quando |
|---|---|
| 400 | Validação do corpo (`@Valid`), produto inativo ou sem preço ativo. |
| 403 | Perfil sem permissão (Caixa e Vendas) ou login inválido. |
| 404 | Recurso não encontrado (`RecursoNaoEncontradoException`). |
| 409 | Estoque alterado ao mesmo tempo (lock otimista) ou comanda ficaria sem itens. |
| 422 | Regra de negócio violada (`RegraNegocioException` e `IllegalArgumentException`). |

Quem pode o quê em Caixa e Vendas (validado dentro dos services): Administrador e Gerente fazem tudo. O
Funcionário abre o caixa, consulta o caixa atual, lança suprimento e opera as vendas, mas não vê o histórico,
não faz sangria nem fechamento e não cancela vendas.

---

## 5. Limitações e pontos de atenção

### 5.1 Duas rotas iguais em `GET /pedido`
`GET /pedido` (listar todas) e `GET /pedido?status=` (listar por status) usam o mesmo caminho e método. O
OpenAPI só permite uma operação por caminho e método, então a tela mostra apenas **Listar comandas por
status**. As duas rotas continuam funcionando na API. Mudar isso exigiria mudar a rota, o que está fora do
escopo.

### 5.2 Login com senha errada devolve 403
Comportamento do Spring Security já existente, sem entry point configurado. O Swagger documenta o 403. Não foi
alterado, porque o combinado foi não mexer em segurança.

### 5.3 Erros sem tratamento dedicado
Alguns caminhos do código antigo lançam `RuntimeException` ou `orElseThrow()` sem mensagem, por exemplo
`PATCH /pedido/{id}/status/{status}` com pedido inexistente e `GET /produto-ingrediente/{produtoId}/{ingredienteId}`
quando o vínculo não existe. O `GlobalExceptionHandler` não trata esses casos, então não são documentados como
resposta esperada. É comportamento anterior a esta branch.

### 5.4 Textos com acentos
Os arquivos estão em UTF-8. Se os acentos aparecerem trocados em algum terminal, é só a exibição do console.

---

## 6. Como validar

```powershell
# testes (251 devem passar)
.\rodar-backend-local.ps1 -Testes

# subir a API e abrir o Swagger
.\rodar-backend-local.ps1
# http://localhost:8080/swagger-ui.html
```

`rodar-backend-local.ps1` fica na raiz do projeto, fora dos repositórios. Ele define `DB_URL`, `DB_USER`,
`DB_PASSWORD` e `JWT_SECRET` só para o processo, porque o `application.properties` exige essas variáveis.

Conferência feita nesta branch:
- 251 testes passando.
- `/v3/api-docs` com 48 caminhos e 75 operações, todas com resumo.
- 12 seções, sem duplicatas e na ordem do fluxo.
- `/usuarios`, `/sec` e `/dispositivos` ausentes do documento.
- Exemplos e campos `READ_ONLY` presentes nos DTOs de Caixa e Venda.