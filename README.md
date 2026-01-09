# Sakany 🏘️

> **A Modular Monolith Spring Boot application for modern residential compound management.**

[![Java](https://img.shields.io/badge/Java-23-orange)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)](https://www.docker.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791)](https://www.postgresql.org)

## ⚡️ Quick Start

**1. Decrypt Configuration (Required)**
This project uses an encrypted secrets file. You must decrypt it before running the app.
```bash
# Ask team lead for the password
openssl enc -aes-256-cbc -pbkdf2 -d -in secrets.enc -out secrets.properties
```

**2. Start Database & Tools**
```bash
docker compose up -d
```
*   **PgAdmin**: [http://localhost:5433](http://localhost:5433) (Login: `admin@sakany.com` / Password: in `secrets.properties`)
*   **Postgres**: Port `5432`

**3. Run Application**
```bash
./gradlew bootRun
```

---

## 🏗️ Architecture

Sakany follows a **Modular Monolith** architecture with Domain-Driven Design (DDD) principles.

*   **`com.theMs.sakany.shared`**: Shared kernel (DDD base classes, value objects).
*   **`com.theMs.sakany.users`**: User management & authentication (Planned).
*   **`com.theMs.sakany.maintenance`**: Maintenance ticket lifecycle (Planned).

---

## 🛠️ Development

### Managing Secrets
The `secrets.properties` file is gitignored. To share changes with the team:

1.  Edit `secrets.properties` locally.
2.  **Encrypt** and commit the new file:
    ```bash
    openssl enc -aes-256-cbc -pbkdf2 -salt -in secrets.properties -out secrets.enc
    ```

### Code Style
*   **Language**: Java 23 (Sequenced Collections, Virtual Threads ready).
*   **Style**: Google Java Style (enforced via Spotless - *planned*).

---

## 📦 Features

*   **For Residents (Mobile)**: QR Code Access, Maintenance Requests, Community Voting.
*   **For Admins (Web)**: Operations Dashboard, Security Logs, Financial Tracking.
