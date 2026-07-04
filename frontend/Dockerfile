# frontend/Dockerfile
FROM node:20-alpine AS build
WORKDIR /app

# Копируем package.json и package-lock.json
COPY frontend/package*.json ./
RUN npm ci --legacy-peer-deps || npm install --legacy-peer-deps

# Копируем весь код
COPY frontend/ .

# Собираем проект
RUN npm run build

# Проверяем, что сборка создала правильную папку
RUN ls -la /app

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Копируем собранные файлы (проверяем возможные пути)
COPY --from=build /app/build /usr/share/nginx/html
# Или если используете Vite:
# COPY --from=build /app/dist /usr/share/nginx/html

# Копируем конфиг Nginx (если есть)
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]