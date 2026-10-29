# Frontend Angular servido pelo nginx. O código vem do repositório
# Easy-Eats, passado como contexto adicional "app" (ver docker-compose.yaml);
# o contexto principal é esta pasta, só para o nginx.conf.
FROM node:22-alpine AS build
WORKDIR /app

COPY --from=app package*.json ./
RUN npm ci

COPY --from=app . .

# Em container o nginx faz proxy de /api para o backend, então o frontend
# chama a API na mesma origem. Passe outra URL em API_URL para apontar para
# uma API externa.
ARG API_URL=/api
RUN sed -i "s#apiUrl: '[^']*'#apiUrl: '${API_URL}'#" src/environments/environment.prod.ts \
    && npm run build

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
