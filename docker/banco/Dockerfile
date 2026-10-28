# PostgreSQL do Easy Eats. O schema é criado pelo Flyway quando a API sobe
# (src/main/resources/db/migration); aqui só fica o banco e os scripts de
# inicialização da pasta init/ (rodam apenas com o volume vazio).
FROM postgres:16-alpine

ENV TZ=America/Sao_Paulo \
    PGTZ=America/Sao_Paulo

COPY init/ /docker-entrypoint-initdb.d/
