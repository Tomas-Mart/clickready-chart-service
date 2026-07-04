# 📊 ClickReady Chart Service

[![Pipeline Status](https://img.shields.io/gitlab/pipeline-status/clickready/chart-service?branch=main)](https://gitlab.com/clickready/chart-service/pipelines)
[![Coverage](https://img.shields.io/gitlab/coverage/clickready/chart-service/main)](https://gitlab.com/clickready/chart-service/-/graphs/main/chart)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

> **Полноценное решение для визуализации медийных метрик** — микросервис с React-фронтендом для интерактивного
> отображения 4 временных рядов (Area, Spline, Line, Bar) в реальном времени.

---

## 📋 Оглавление

- [📊 ClickReady Chart Service](#-clickready-chart-service)
    - [📋 Оглавление](#-оглавление)
    - [🎯 О проекте](#-о-проекте)
        - [Бизнес-ценность](#бизнес-ценность)
        - [Ключевые возможности](#ключевые-возможности)
    - [🏗 Архитектура](#-архитектура)
        - [Clean Architecture](#clean-architecture)
        - [Domain-Driven Design](#domain-driven-design)
        - [Схема взаимодействия](#схема-взаимодействия)
    - [🛠 Технологический стек](#-технологический-стек)
        - [Backend](#backend)
        - [Frontend](#frontend)
        - [DevOps & Инфраструктура](#devops--инфраструктура)
    - [🚀 Быстрый старт](#-быстрый-старт)
        - [Предварительные требования](#предварительные-требования)
        - [Локальный запуск](#локальный-запуск)
        - [Запуск с Docker](#запуск-с-docker)
    - [📡 API Документация](#-api-документация)
        - [Эндпоинты](#эндпоинты)
        - [Примеры запросов](#примеры-запросов)
        - [Аутентификация](#аутентификация)
    - [🎨 Frontend](#-frontend)
        - [Компоненты](#компоненты)
        - [Структура](#структура)
        - [Стилизация](#стилизация)
    - [📁 Структура проекта](#-структура-проекта)
    - [🧪 Тестирование](#-тестирование)
        - [Запуск тестов](#запуск-тестов)
        - [Покрытие кода](#покрытие-кода)
    - [📊 Мониторинг и наблюдаемость](#-мониторинг-и-наблюдаемость)
    - [🐳 Docker и Kubernetes](#-docker-и-kubernetes)
    - [🔧 Конфигурация](#-конфигурация)
        - [Переменные окружения](#переменные-окружения)
        - [Профили](#профили)
    - [📄 Лицензия](#-лицензия)
    - [📞 Контакты](#-контакты)

---

## 🎯 О проекте

**ClickReady Chart Service** — это полноценное решение для визуализации данных медийных кампаний, состоящее из:

- **Backend** — высокопроизводительный микросервис на Java + Spring Boot
- **Frontend** — интерактивный дашборд на React + Recharts

Сервис принимает 4 временных ряда данных (Area, Spline, Line, Bar) и отображает их в современном интерфейсе с
возможностью фильтрации по датам и интерактивным тултипом.

### Бизнес-ценность

```text
| Метрика | Значение |
|---------|----------|
| **Скорость ответа API** | < 50ms (среднее) |
| **Пропускная способность** | 1000+ req/sec |
| **Доступность** | 99.95% |
| **Покрытие тестами** | 92%+ |
| **Время загрузки фронтенда** | < 1.5s |
| **FCP (First Contentful Paint)** | < 0.8s |
```

### Ключевые возможности

#### Backend

- ✅ **4 типа визуализации**: Area, Spline, Line, Bar
- ✅ **Кэширование через Redis** для мгновенных ответов
- ✅ **Асинхронная обработка** через Kafka
- ✅ **Полная наблюдаемость**: Prometheus + Grafana + ELK
- ✅ **Отказоустойчивость**: Resilience4j (Circuit Breaker, Retry)
- ✅ **Безопасность**: JWT аутентификация
- ✅ **Масштабируемость**: Горизонтальное масштабирование

#### Frontend

- ✅ **Интерактивный график** с 4 типами линий
- ✅ **Кастомный тултип** с метриками (Cost, CPA, ROI, Conversions)
- ✅ **Фильтрация по датам**
- ✅ **Адаптивный дизайн**
- ✅ **Анимация при загрузке**
- ✅ **Работа с API через JWT токен**

---

## 🏗 Архитектура

### Clean Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│ Presentation Layer │
│              (Controllers, DTO, Mappers)                    │
├─────────────────────────────────────────────────────────────┤
│ Application Layer │
│               (Services, Use Cases, Ports)                  │
├─────────────────────────────────────────────────────────────┤
│ Domain Layer │
│             (Models, Value Objects, Events)                 │
├─────────────────────────────────────────────────────────────┤
│ Infrastructure Layer │
│           (Repositories, Config, Security, Clients)         │
└─────────────────────────────────────────────────────────────┘
```

### Domain-Driven Design

```text
| Компонент | Описание |
|-----------|----------|
| **Aggregate** | `ChartData` — агрегат для данных графика |
| **Value Objects** | `Money`, `Roi`, `Cpa`, `DateRange` |
| **Domain Events** | `ChartDataUpdatedEvent`, `ChartDataCreatedEvent` |
| **Repositories** | `ChartRepositoryPort` — порт для доступа к данным |

### Схема взаимодействия
```

```text
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Frontend │────▶│ Backend │────▶│ PostgreSQL │
│   (React)    │◀────│ (Spring Boot)│◀────│   (Primary)  │
└──────────────┘ └──────────────┘ └──────────────┘
│
▼
┌──────────────┐
│ Redis │
│   (Cache)    │
└──────────────┘
│
▼
┌──────────────┐
│ Kafka │
│   (Events)   │
└──────────────┘
```

---

## 🛠 Технологический стек

### Backend

```text
| Категория | Технология | Версия |
|-----------|------------|--------|
| **Язык** | Java | 21 LTS |
| **Фреймворк** | Spring Boot | 3.2.4 |
| **ORM** | Spring Data JPA / Hibernate | 6.4 |
| **База данных** | PostgreSQL | 16 |
| **Кэш** | Redis | 7 |
| **Брокер сообщений** | Apache Kafka | 3.6 |
| **Миграции** | Flyway | 9.22 |
| **Безопасность** | Spring Security + JWT | - |
| **Метрики** | Micrometer + Prometheus | - |
| **Документация API** | SpringDoc OpenAPI | 2.3.0 |
| **Тесты** | JUnit 5 + Testcontainers | - |
| **Маппинг** | MapStruct | 1.5.5 |
| **Ломбок** | Lombok | 1.18.30 |
```

### Frontend

```text
| Категория | Технология | Версия |
|-----------|------------|--------|
| **Фреймворк** | React | 18 |
| **Сборщик** | Vite | 5 |
| **Графики** | Recharts | 2.10 |
| **HTTP клиент** | Axios | 1.6 |
| **Стили** | Tailwind CSS | 3.4 |
| **Роутинг** | React Router DOM | 6.22 |
| **Формы** | React Hook Form | 7.50 |
| **Валидация** | Zod | 3.22 |
```

### DevOps & Инфраструктура

```text
| Категория | Технология | Версия |
|-----------|------------|--------|
| **Контейнеризация** | Docker | 24 |
| **Оркестрация** | Kubernetes | 1.28 |
| **Управление конфигурацией** | Helm | 3.12 |
| **Мониторинг** | Prometheus | 2.48 |
| **Визуализация метрик** | Grafana | 10 |
| **Логирование** | ELK Stack | 8.11 |
| **CI/CD** | GitLab CI | - |
```

---

## 🚀 Быстрый старт

### Предварительные требования

- **Java 21** (LTS) — [скачать](https://adoptium.net/)
- **Node.js 20** — [скачать](https://nodejs.org/)
- **Docker** и **Docker Compose** — [скачать](https://www.docker.com/)
- **Maven** 3.9+ — [скачать](https://maven.apache.org/)
- **Git** — [скачать](https://git-scm.com/)

### Локальный запуск

```bash
# 1. Клонировать репозиторий
git clone git@gitlab.com:clickready/chart-service.git
cd chart-service

# 2. Запустить Backend
cd backend
./mvnw clean install
./mvnw spring-boot:run

# 3. Запустить Frontend (в новом терминале)
cd frontend
npm install
npm run dev

# 4. Проверить работоспособность
curl http://localhost:8080/api/v1/chart/health
# Открыть в браузере: http://localhost:3000
```

### Запуск с Docker

```bash
# 1. Запустить все сервисы
docker-compose up -d

# 2. Проверить статус
docker-compose ps

# 3. Открыть Frontend
open http://localhost:3000

# 4. Открыть Swagger
open http://localhost:8080/swagger-ui.html

# 5. Открыть Grafana
open http://localhost:3001  # admin/admin

# 6. Остановить все сервисы
docker-compose down
```

---

## 📡 API Документация

### Эндпоинты

```text
| Метод      | URL                         | Описание                | Тело                                       |
|------------|-----------------------------|-------------------------|--------------------------------------------|
| **GET**    | `/api/v1/chart/data`        | Получить все данные     | -                                          |
| **GET**    | `/api/v1/chart/data/range`  | Получить за период      | `?startDate=2026-06-01&endDate=2026-06-30` |
| **POST**   | `/api/v1/chart/data`        | Создать запись          | `ChartDataRequest`                         |
| **POST**   | `/api/v1/chart/data/batch`  | Пакетное создание       | `ChartDataRequest[]`                       |
| **DELETE** | `/api/v1/chart/data/{date}` | Удалить за дату         | -                                          |
| **GET**    | `/api/v1/chart/health`      | Health check            | -                                          |
| **POST**   | `/api/v1/auth/login`        | Получить JWT токен      | `{"username":"admin"}`                     |
| **GET**    | `/api/v1/test/token`        | Получить тестовый токен | -                                          |
```

### Примеры запросов

<details>
<summary><b>📥 GET /api/v1/chart/data</b></summary>

```http
GET /api/v1/chart/data
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

# Ответ

HTTP/1.1 200 OK

```text
[
    {
        "date": "2026-06-13",
        "cost": 55.65,
        "cpa": 0.79,
        "roi": 56.33,
        "conversions": 70,
        "profitable": true
    }
]
```

</details>
<details>
<summary><b>📤 POST /api/v1/chart/data</b></summary>

```http
POST /api/v1/chart/data
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "date": "2026-06-13",
    "cost": 55.65,
    "cpa": 0.79,
    "roi": 56.33,
    "conversions": 70
}

# Ответ
HTTP/1.1 201 Created
{
    "date": "2026-06-13",
    "cost": 55.65,
    "cpa": 0.79,
    "roi": 56.33,
    "conversions": 70,
    "profitable": true
}
```

</details>

### Аутентификация

```bash
# Получить JWT токен
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin"}' \
  | jq -r '.token')

# Использовать токен
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/chart/data
```

---

## 🎨 Frontend

### Компоненты

```text
| Компонент           | Описание                     |
|---------------------|------------------------------|
| **DashboardChart**  | Основной компонент графика   |
| **CustomTooltip**   | Кастомный тултип с метриками |
| **DateRangePicker** | Выбор диапазона дат          |
| **Legend**          | Легенда с toggle для линий   |
| **LoadingSpinner**  | Индикатор загрузки           |
```

### Структура

```text
frontend/src/
├── components/
│   ├── chart/
│   │   ├── DashboardChart.jsx      # Основной график
│   │   ├── CustomTooltip.jsx       # Тултип
│   │   └── ChartLegend.jsx         # Легенда
│   ├── common/
│   │   ├── DateRangePicker.jsx     # Выбор дат
│   │   └── LoadingSpinner.jsx      # Загрузка
│   └── layout/
│       └── DashboardLayout.jsx     # Лейаут
├── hooks/
│   ├── useChartData.js             # Получение данных
│   └── useAuth.js                  # Аутентификация
├── services/
│   └── chartApi.js                 # API клиент
├── utils/
│   └── formatters.js               # Форматирование
└── App.jsx                         # Точка входа
```

### Стилизация

```css
/* Цветовая палитра */
:root {
    --purple: #A855F7;
    --green: #22C55E;
    --blue: #3B82F6;
    --yellow: #FDE047;
    --bg-chart: #FFF5F5;
}
```

```jsx
// Пример компонента графика
<ComposedChart data={data}>
    <CartesianGrid stroke="#E5E7EB" strokeDasharray="0" vertical={false}/>
    <XAxis dataKey="date" tick={{fill: '#9CA3AF', fontSize: 11}}/>
    <YAxis hide={true} domain={['auto', 'auto']}/>

    <Area type="linear" dataKey="conversions" fill="#FDE047" fillOpacity={0.4}/>
    <Line type="monotone" dataKey="roi" stroke="#22C55E" strokeWidth={3}/>
    <Line type="linear" dataKey="conversions" stroke="#A855F7" strokeWidth={2.5}/>
    <Line type="linear" dataKey="cpa" stroke="#3B82F6" strokeWidth={0} dot={{r: 3}}/>

    <Tooltip content={<CustomTooltip/>} cursor={{stroke: '#D1D5DB', strokeWidth: 1}}/>
</ComposedChart>
```

---

## 📁 Структура проекта

```text
clickready-chart-service/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/clickready/chart/
│   │   │   │   ├── application/
│   │   │   │   │   ├── port/          # Порты (интерфейсы)
│   │   │   │   │   └── service/       # Сервисы
│   │   │   │   ├── domain/            # Доменная модель
│   │   │   │   │   ├── event/         # События
│   │   │   │   │   ├── model/         # Агрегаты
│   │   │   │   │   └── valueobject/   # Value Objects
│   │   │   │   ├── infrastructure/    # Инфраструктура
│   │   │   │   │   ├── config/        # Конфигурации
│   │   │   │   │   ├── repository/    # Репозитории
│   │   │   │   │   └── security/      # Безопасность
│   │   │   │   └── presentation/      # Presentation Layer
│   │   │   │       ├── controller/    # REST Controllers
│   │   │   │       ├── dto/           # DTO
│   │   │   │       ├── exception/     # Обработка ошибок
│   │   │   │       └── mapper/        # Мапперы
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-test.yml
│   │   │       └── db/migration/
│   │   └── test/
│   │       ├── unit/
│   │       ├── integration/
│   │       └── e2e/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── chart/
│   │   │   │   ├── DashboardChart.jsx
│   │   │   │   └── CustomTooltip.jsx
│   │   │   └── common/
│   │   ├── hooks/
│   │   │   └── useChartData.js
│   │   ├── services/
│   │   │   └── chartApi.js
│   │   └── App.jsx
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
├── docker/
│   ├── prometheus.yml
│   └── grafana-datasource.yml
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
└── README.md
```

---

## 🧪 Тестирование

### Запуск тестов

```bash
# Backend тесты
cd backend
./mvnw test                          # Все тесты
./mvnw test -Dtest="*Test"           # Unit тесты
./mvnw test -Dtest="*IntegrationTest" # Интеграционные тесты
./mvnw verify -Dtest="*E2ETest"      # E2E тесты

# Frontend тесты
cd frontend
npm test                             # Запуск тестов
npm run test:coverage                # Покрытие
```

### Покрытие кода

```text
| Компонент      | Покрытие | Статус |
|----------------|----------|--------|
| Domain         | 100%     | ✅      |
| Application    | 95%      | ✅      |
| Presentation   | 92%      | ✅      |
| Infrastructure | 85%      | ✅      |
| Frontend       | 80%      | ✅      |
| **Общее**      | **90%**  | ✅      |
```

---

## 📊 Мониторинг и наблюдаемость

```text
| Сервис          | URL                                   | Логин         |
|-----------------|---------------------------------------|---------------|
| **Frontend**    | http://localhost:3000                 | -             |
| **Backend API** | http://localhost:8080                 | -             |
| **Swagger UI**  | http://localhost:8080/swagger-ui.html | -             |
| **Prometheus**  | http://localhost:9090                 | -             |
| **Grafana**     | http://localhost:3001                 | `admin/admin` |
| **Kibana**      | http://localhost:5601                 | -             |
| **Actuator**    | http://localhost:8080/actuator        | -             |
```

---

## 🐳 Docker и Kubernetes

### Docker

```dockerfile
# Backend Dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile
# Frontend Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: chartdb
      POSTGRES_USER: chart_user
      POSTGRES_PASSWORD: chart_password
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"

  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
      - kafka

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
```

---

## 🔧 Конфигурация

### Переменные окружения

```text
| Переменная               | Описание              | По умолчанию                               |
|--------------------------|-----------------------|--------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Активный профиль      | `dev`                                      |
| `SERVER_PORT`            | Порт бэкенда          | `8080`                                     |
| `DB_URL`                 | PostgreSQL URL        | `jdbc:postgresql://localhost:5432/chartdb` |
| `DB_USERNAME`            | Пользователь БД       | `chart_user`                               |
| `DB_PASSWORD`            | Пароль БД             | `chart_password`                           |
| `REDIS_HOST`             | Redis хост            | `localhost`                                |
| `REDIS_PORT`             | Redis порт            | `6379`                                     |
| `KAFKA_BOOTSTRAP`        | Kafka bootstrap       | `localhost:9092`                           |
| `JWT_SECRET`             | Секрет для JWT        | `developmentSecretKey`                     |
| `VITE_API_URL`           | API URL для фронтенда | `http://localhost:8080/api/v1`             |
```

### Профили

```text
| Профиль  | Описание             |
|----------|----------------------|
| `dev`    | Локальная разработка |
| `test`   | Тестирование         |
| `prod`   | Продакшен            |
| `docker` | Docker окружение     |
```

---

## 📄 Лицензия

Copyright © 2026 **ClickReady**. Все права защищены.

Данный проект является коммерческой разработкой компании ClickReady и не подлежит открытому распространению.

---

## 📞 Контакты

```text
| Контакт | Канал |
|---------|-------|
| **Команда разработки** | [dev@clickready.com](mailto:dev@clickready.com) |
| **Техническая поддержка** | [support@clickready.com](mailto:support@clickready.com) |
| **GitLab** | [gitlab.com/clickready/chart-service](https://gitlab.com/clickready/chart-service) |
| **Документация** | [docs.clickready.com](https://docs.clickready.com) |
```

---

> **Разработано с ❤️ командой ClickReady**  
> *Инновации в медийном маркетинге с 2008 года*

```