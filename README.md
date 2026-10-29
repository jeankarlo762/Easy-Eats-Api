# Easy Eats API

API REST do Easy Eats (Spring Boot 4 / Java 17+ / PostgreSQL), com o ambiente
completo — banco, backend e frontend — em containers Docker.

## Estrutura

```
Easy-Eats-Api/
├── src/                               código da API
│   └── main/resources/db/migration/   scripts SQL de criação do banco (Flyway V1…V17)
├── docker/
│   ├── banco/        Dockerfile do PostgreSQL + README com os scripts do banco
│   ├── backend/      Dockerfile da API (build Maven → JRE 21)
│   ├── frontend/     Dockerfile do Angular servido pelo nginx
│   ├── docker-compose.yaml
│   ├── .env.example  variáveis de ambiente (copiar para .env)
│   └── README.md     detalhes do ambiente Docker e publicação no Docker Hub
└── pom.xml
```

O frontend fica no repositório
[Easy-Eats](https://github.com/jeankarlo762/Easy-Eats), clonado ao lado deste:

```
Projeto/
├── Easy-Eats/       (frontend)
└── Easy-Eats-Api/   (este repositório)
```

## Pré-requisitos

- Docker e Docker Compose (Docker Desktop no Windows/macOS)
- Git

## Como executar

```sh
git clone https://github.com/jeankarlo762/Easy-Eats.git
git clone https://github.com/jeankarlo762/Easy-Eats-Api.git
cd Easy-Eats-Api/docker
cp .env.example .env          # no Windows: copy .env.example .env
```

Edite o `.env` e preencha pelo menos:

| Variável | Para que serve |
|---|---|
| `DB_PASSWORD` | senha do PostgreSQL |
| `JWT_SECRET` | segredo dos tokens de login (`openssl rand -base64 64`) |

Depois suba os três containers:

```sh
docker compose up -d --build
docker compose ps
```

| Serviço | Serviço no compose | Acesso |
|---|---|---|
| Frontend | `frontend` (nginx) | http://localhost:4200 |
| API | `backend` (Spring Boot) | http://localhost:8080 |
| Banco | `banco` (PostgreSQL 16) | localhost:5432 |

Login de demonstração: `admin@easyeats.com` / `admin123`.

## Como os containers se comunicam

O `docker-compose.yaml` declara a rede `easy-eats-net`. Os três serviços estão
nela e se chamam pelo nome do serviço:

- o backend conecta no banco em `jdbc:postgresql://banco:5432/easyeats`;
- o nginx do frontend encaminha `/api` para `http://backend:8080`.

## Volumes e variáveis de ambiente

- Os dados do PostgreSQL ficam no volume `banco-dados` e sobrevivem à
  recriação dos containers.
- Senhas e segredos vêm do arquivo `docker/.env`, que não vai para o git
  (`docker/.env.example` é o modelo).

## Banco de dados

As tabelas são criadas pelo Flyway quando a API sobe pela primeira vez, a
partir dos scripts em `src/main/resources/db/migration`. A lista dos scripts e
o que cada um faz está em [`docker/banco/README.md`](docker/banco/README.md).

## Comandos úteis

```sh
docker compose logs -f backend         # logs da API
docker compose up -d --build frontend  # rebuild de um serviço
docker compose down                    # para tudo (mantém os dados)
docker compose down -v                 # para e APAGA o volume do banco
```

## Rodar a API sem Docker

Com um PostgreSQL local e Java 17 ou superior:

```sh
./mvnw spring-boot:run
```

As variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` configuram a
conexão (ver `src/main/resources/application.properties`).
