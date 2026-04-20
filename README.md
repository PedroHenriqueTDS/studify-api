#  Studify API

> Sistema de Gerenciamento de Estudos — Back-end Java com Spring Boot

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

##  Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Como Rodar](#como-rodar)
- [Endpoints](#endpoints)
- [Deploy](#deploy)
- [Variáveis de Ambiente](#variáveis-de-ambiente)

---

## Sobre o Projeto

A **Studify API** é um back-end RESTful para gerenciamento de estudos. O usuário pode cadastrar matérias, registrar sessões de estudo com cronômetro, definir metas de horas e organizar tarefas por prioridade — tudo protegido com autenticação JWT.

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Security | 6 | Autenticação e autorização |
| JWT (jjwt) | 0.11.5 | Tokens de acesso |
| Spring Data JPA | 3.2 | Persistência |
| Hibernate | 6 | ORM |
| MySQL | 8.0 | Banco de dados |
| springdoc-openapi | 2.5 | Swagger UI |
| Lombok | latest | Redução de boilerplate |
| Docker | latest | Containerização |
| Maven | 3.9 | Build |

---

## Arquitetura

```
com.studify
├── config/          # SecurityConfig, SwaggerConfig
├── controller/      # REST Controllers (AuthController, SubjectController...)
├── dto/             # Records de Request/Response por domínio
├── entity/          # Entidades JPA (User, Subject, StudySession, Goal, Task)
├── exception/       # GlobalExceptionHandler + exceções customizadas
├── repository/      # Interfaces JpaRepository com queries JPQL
├── security/        # JwtService, JwtAuthFilter, UserDetailsServiceImpl
└── service/         # Regras de negócio (AuthService, SubjectService...)
```

**Fluxo de autenticação:**
```
Cliente → POST /auth/login → AuthController
       → AuthService → AuthenticationManager
       → JwtService (gera access + refresh token)
       → Retorna AuthResponse com Bearer JWT

Requisições protegidas:
Cliente → Header: Authorization: Bearer <token>
       → JwtAuthFilter → JwtService.isTokenValid()
       → SecurityContext → Controller → Service
```

---

## Funcionalidades

###  Autenticação
- Registro de usuário com senha criptografada (BCrypt)
- Login com JWT (access token 24h + refresh token 7 dias)
- Renovação de token via refresh token

###  Matérias (Subjects)
- CRUD completo
- Cor personalizada (HEX)
- Total de minutos estudados por matéria

### ️ Sessões de Estudo
- Iniciar sessão (tempo real) ou registrar já finalizada
- Finalizar sessão com cálculo automático de duração
- Resumo total de horas e média por sessão
- Filtro por período e por matéria

###  Metas
- Definir meta de horas por matéria ou geral
- Adicionar progresso gradual
- Status automático: `IN_PROGRESS` → `COMPLETED` ao atingir a meta
- Filtro por status

###  Tarefas
- CRUD com prioridade (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- Status: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- Endpoint `PATCH /tasks/{id}/complete` para concluir rapidamente
- Filtros por status e matéria

###  Dashboard
- Resumo em um endpoint: matérias, horas totais, metas e tarefas

---

## Como Rodar

### Pré-requisitos

- Docker + Docker Compose **ou** Java 17 + MySQL 8

###  Com Docker (recomendado)

```bash
# 1. Clone o projeto
git clone https://github.com/seu-usuario/studify.git
cd studify

# 2. Configure as variáveis (opcional, tem valores padrão)
cp .env.example .env

# 3. Suba tudo com um comando
docker compose up --build

# API disponível em: http://localhost:8080
# Swagger UI em:     http://localhost:8080/swagger-ui.html
```

###  Sem Docker (local)

```bash
# 1. Crie o banco MySQL
mysql -u root -p -e "CREATE DATABASE studify_db;"

# 2. Configure application.yml ou exporte variáveis
export DB_URL=jdbc:mysql://localhost:3306/studify_db?...
export DB_USERNAME=root
export DB_PASSWORD=sua_senha

# 3. Rode
./mvnw spring-boot:run
```

---

## Endpoints

###  Auth
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/register` | Cadastrar usuário |
| POST | `/api/v1/auth/login` | Login → retorna JWT |
| POST | `/api/v1/auth/refresh-token` | Renovar access token |

###  Matérias
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/subjects` | Criar matéria |
| GET | `/api/v1/subjects` | Listar (paginado) |
| GET | `/api/v1/subjects/{id}` | Buscar por ID |
| PUT | `/api/v1/subjects/{id}` | Atualizar |
| DELETE | `/api/v1/subjects/{id}` | Deletar |

###  Sessões
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/study-sessions` | Criar sessão |
| GET | `/api/v1/study-sessions` | Listar (filtro: subjectId) |
| GET | `/api/v1/study-sessions/{id}` | Buscar por ID |
| PATCH | `/api/v1/study-sessions/{id}/finish` | Finalizar sessão |
| PUT | `/api/v1/study-sessions/{id}` | Atualizar |
| DELETE | `/api/v1/study-sessions/{id}` | Deletar |
| GET | `/api/v1/study-sessions/summary` | Resumo geral |
| GET | `/api/v1/study-sessions/summary/period` | Resumo por período |

###  Metas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/goals` | Criar meta |
| GET | `/api/v1/goals` | Listar (filtro: status) |
| GET | `/api/v1/goals/{id}` | Buscar por ID |
| PUT | `/api/v1/goals/{id}` | Atualizar |
| PATCH | `/api/v1/goals/{id}/progress?hours=2` | Adicionar progresso |
| DELETE | `/api/v1/goals/{id}` | Deletar |

###  Tarefas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/tasks` | Criar tarefa |
| GET | `/api/v1/tasks` | Listar (filtros: status, subjectId) |
| GET | `/api/v1/tasks/{id}` | Buscar por ID |
| PUT | `/api/v1/tasks/{id}` | Atualizar |
| PATCH | `/api/v1/tasks/{id}/complete` | Concluir tarefa |
| DELETE | `/api/v1/tasks/{id}` | Deletar |

###  Dashboard
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/dashboard` | Resumo geral do usuário |

> Todos os endpoints (exceto `/auth/**`) exigem o header:
> `Authorization: Bearer <token>`

---

## Deploy

### Render

```bash
# 1. Crie uma conta em render.com
# 2. Conecte seu repositório GitHub
# 3. O arquivo render.yaml já configura tudo automaticamente
#    (Web Service + MySQL)
# 4. Em "Environment", adicione:
#    JWT_SECRET = (gere com: openssl rand -hex 32)
```

### Railway

```bash
# 1. Instale o CLI: npm install -g @railway/cli
# 2. Login
railway login

# 3. Inicie o projeto
railway init

# 4. Adicione MySQL
railway add --plugin mysql

# 5. Configure as variáveis no painel Railway:
#    DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET

# 6. Deploy
railway up
```

---

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/studify_db...` | URL do banco |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | `root` | Senha do banco |
| `JWT_SECRET` | (valor padrão inseguro) | Chave secreta JWT — **troque em produção!** |
| `PORT` | `8080` | Porta da aplicação |
| `DDL_AUTO` | `update` | Estratégia Hibernate DDL |
| `SHOW_SQL` | `true` | Exibir SQL no console |

---

##  Swagger UI

Acesse após subir a aplicação:

```
http://localhost:8080/swagger-ui.html
```

1. Clique em **POST /api/v1/auth/register** → cadastre-se
2. Clique em **POST /api/v1/auth/login** → copie o `accessToken`
3. Clique em **Authorize** () → cole `Bearer <token>`
4. Todos os endpoints ficam liberados

---

## Licença

Distribuído sob licença MIT. Veja [LICENSE](LICENSE) para mais informações.
