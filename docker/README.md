# Docker — Easy Eats

Banco, backend e frontend rodam em containers separados, cada um com sua pasta:

```
docker/
├── banco/       PostgreSQL 16 (+ init/: scripts que rodam na criação do volume)
├── backend/     API Spring Boot (build Maven → JRE 21)
├── frontend/    Angular buildado e servido pelo nginx (proxy /api → backend)
├── docker-compose.yml
├── .env.example
└── publicar.sh  build + push para o Docker Hub
```

O frontend fica no repositório **Easy-Eats**, clonado ao lado deste:

```
Projeto/
├── Easy-Eats/       (frontend)
└── Easy-Eats-Api/   (este repositório)
```

## Subir o projeto

```sh
cd docker
cp .env.example .env      # preencha DB_PASSWORD e JWT_SECRET (openssl rand -base64 64)
docker compose up -d --build
```

| Serviço  | URL                    |
|----------|------------------------|
| Frontend | http://localhost:4200  |
| API      | http://localhost:8080  |
| Banco    | localhost:5432         |

O schema é criado pelo Flyway na primeira subida da API; os seeders criam a
empresa demo (`admin@easyeats.com` / `admin123`).

Comandos úteis:

```sh
docker compose logs -f backend     # logs da API
docker compose up -d --build frontend   # rebuild só de um serviço
docker compose down                # para tudo (mantém os dados)
docker compose down -v             # para e APAGA o volume do banco
```

## Docker Hub

As imagens vão para um único repositório, `<DOCKERHUB_USERNAME>/easy-eats`, com
uma tag por serviço:

- `easy-eats:banco-<tag>`
- `easy-eats:backend-<tag>`
- `easy-eats:frontend-<tag>`

O repositório é criado automaticamente no primeiro push (público por padrão;
para privado, crie antes em hub.docker.com > Create repository).

**Pela máquina local:** defina `DOCKERHUB_USERNAME` no `.env`, rode `docker login`
e depois `./publicar.sh` (ou `./publicar.sh v1.0`).

**Pelo GitHub Actions:** cadastre os secrets `DOCKERHUB_USERNAME` e
`DOCKERHUB_TOKEN` no repositório Easy-Eats-Api. O workflow
`.github/workflows/docker-publish.yml` publica a cada push em `develop`/`main`
(tags = nome do branch e hash curto do commit) e pode ser rodado manualmente.

Para rodar a partir das imagens publicadas, sem build:

```sh
docker compose pull && docker compose up -d --no-build
```
