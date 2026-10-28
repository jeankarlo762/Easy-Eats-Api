#!/bin/sh
# Roda uma única vez, quando o volume do banco é criado.
set -e
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "ALTER DATABASE \"$POSTGRES_DB\" SET timezone TO 'America/Sao_Paulo';"
