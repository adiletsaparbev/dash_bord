package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DashBordApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashBordApplication.class, args);
	}
}


//(c) Корпорация Майкрософт (Microsoft Corporation). Все права защищены.
//
//		C:\Users\Acer>netstat -ano | findstr :8080
//TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       20964
//TCP    [::]:8080              [::]:0                 LISTENING       20964

//C:\Users\Acer>taskkill /F /PID 20964
//Успешно: Процесс, с идентификатором 20964, успешно завершен.


// manager  -eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hZ2VyQGdtYWlsIiwicm9sZSI6Ik1BTkFHRVIiLCJpYXQiOjE3NzM4MTM5MTIsImV4cCI6MTc3MzkwMDMxMn0.CDe90ZCoE36wadXrWXKI07FlZ9Y_347n-IVlhzre7BQ

// pm -eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwbUBnbWFpbCIsInJvbGUiOiJQTSIsImlhdCI6MTc3MzgxNjAwNywiZXhwIjoxNzczOTAyNDA3fQ.8OTbOkcO4zsnBT8F03BX2JjB0zRSOwsgVHNXXVi0uw4


/*
Расскажу пошагово как задеплоить Spring Boot + PostgreSQL на Render.com.Вижу структуру проекта. Теперь пишу полное пошаговое руководство.

---

# Деплой Spring Boot на Render.com — пошагово

Render требует **Dockerfile** для Java проектов. Без Docker не обойтись, но это очень просто — я дам готовые файлы, просто скопируй.

---

## Шаг 1 — Создай `Dockerfile` в корне проекта

В корне репозитория (там где `pom.xml`) создай файл с именем `Dockerfile` (без расширения):

```dockerfile
# Стадия 1: сборка проекта
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Стадия 2: запуск
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Папка для загрузки файлов
RUN mkdir -p /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Шаг 2 — Измени `application.properties`

Render будет передавать настройки через **переменные окружения**. Измени файл `src/main/resources/application.properties` вот так:

```properties
spring.application.name=dash_bord
server.port=${PORT:8080}

# БД — берётся из переменных окружения Render
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# File upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=${UPLOAD_DIR:/app/uploads}
```

---

## Шаг 3 — Запушь изменения на GitHub

```bash
git add Dockerfile
git add src/main/resources/application.properties
git commit -m "Add Dockerfile and env-based config for Render"
git push
```

---

## Шаг 4 — Создай PostgreSQL базу данных на Render

1. Открой [dashboard.render.com](https://dashboard.render.com)
2. Нажми кнопку **"New +"** → выбери **"PostgreSQL"**
3. Заполни:
   - **Name:** `dash-bord-db` (любое название)
   - **Region:** выбери ближайший (например Frankfurt)
   - **Plan:** Free
4. Нажми **"Create Database"**
5. Подожди 1-2 минуты пока создастся
6. После создания открой базу и скопируй эти значения (они понадобятся на шаге 6):
   - **Host**
   - **Database**
   - **Username**
   - **Password**
   - **Internal Database URL** (строка вида `postgresql://user:pass@host/db`)

---

## Шаг 5 — Создай Web Service на Render

1. Нажми **"New +"** → **"Web Service"**
2. Выбери **"Build and deploy from a Git repository"** → нажми **Next**
3. Подключи GitHub если не подключён → найди репозиторий `adiletsaparbev/project` → нажми **Connect**
4. Заполни настройки:
   - **Name:** `dash-bord` (любое)
   - **Region:** тот же что у базы
   - **Branch:** `main`
   - **Runtime:** выбери **Docker**
   - **Plan:** Free

---

## Шаг 6 — Добавь переменные окружения

Прокрути вниз до раздела **"Environment Variables"** и добавь:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `jdbc:postgresql://ХОС_ИЗ_RENDER:5432/ИМЯ_БД` |
| `DB_USERNAME` | Username из Render PostgreSQL |
| `DB_PASSWORD` | Password из Render PostgreSQL |
| `JWT_SECRET` | любая строка длиной 32+ символа, например `my_super_secret_key_for_render_2024` |
| `UPLOAD_DIR` | `/app/uploads` |

> ⚠️ **Важно:** `DATABASE_URL` для Spring Boot должна начинаться с `jdbc:postgresql://` — не используй строку в формате `postgres://`, это другой формат. Возьми **Host**, **Database**, **Username**, **Password** по отдельности из Render и собери URL сам:
> ```
> jdbc:postgresql://<host>/<database>
> ```

---

## Шаг 7 — Запусти деплой

Нажми кнопку **"Create Web Service"** внизу страницы.

Render начнёт сборку. В разделе **Logs** увидишь процесс. Первая сборка занимает **5-10 минут**.

Следи за логами — если всё ок, увидишь:
```
Started DashBordApplication in X seconds
```

---

## Возможные проблемы и решения

**Проблема:** `Unable to acquire JDBC Connection`
**Решение:** Проверь переменную `DATABASE_URL` — скорее всего неправильный формат. Должно быть точно `jdbc:postgresql://host/dbname`

---

**Проблема:** `Port already in use` или приложение не запускается
**Решение:** Убедись что в `application.properties` стоит `server.port=${PORT:8080}` — Render сам задаёт PORT

---

**Проблема:** Free план засыпает через 15 минут бездействия
**Решение:** Это нормально для бесплатного плана Render. При первом запросе после сна сервис просыпается ~30 секунд.

---

## Итого — что нужно сделать

1. ✅ Создать `Dockerfile` в корне проекта
2. ✅ Обновить `application.properties` на переменные окружения
3. ✅ Запушить на GitHub
4. ✅ Создать PostgreSQL на Render
5. ✅ Создать Web Service на Render, подключить репозиторий
6. ✅ Добавить переменные окружения
7. ✅ Нажать Deploy

После успешного деплоя твой API будет доступен по адресу вида `https://dash-bord.onrender.com`
 */
