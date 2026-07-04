# Многоступенчатая сборка Docker образа

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости для кэширования
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем необходимые пакеты
RUN apk add --no-cache curl

# Создаем пользователя приложения
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# ============================================================
# ✅ ДОБАВЛЕННЫЕ СТРОКИ (создаём папку logs до переключения на пользователя)
# ============================================================
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app
# ============================================================

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

# Даем права на выполнение
RUN chown -R appuser:appgroup /app

# Переключаемся на пользователя
USER appuser

# Экспортируем порт
EXPOSE 8080

# Точка входа
ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1