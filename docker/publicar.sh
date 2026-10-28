#!/bin/sh
# Build e push das três imagens para o Docker Hub a partir da máquina local.
#   docker login                      (uma vez, com usuário + access token)
#   ./publicar.sh [tag]               (padrão: TAG do .env ou "latest")
# O repositório <DOCKERHUB_USERNAME>/easy-eats é criado no primeiro push.
set -e
cd "$(dirname "$0")"

[ -f .env ] || { echo "Crie docker/.env a partir de .env.example"; exit 1; }
[ -n "$1" ] && export TAG="$1"

docker compose build
docker compose push
