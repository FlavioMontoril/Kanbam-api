# Kanban API

Uma API RESTful de alta performance desenvolvida em **Java 21** e **Spring Boot** para o gerenciamento de quadros Kanban. A aplicação faz parte de uma arquitetura orientada a eventos (Event-Driven Architecture) integrada a um microserviço de autenticação (`auth-server`), contando com segurança via **JWT**, sincronização em tempo real via **WebSockets (STOMP)**, consumo de eventos de usuários via **Apache Kafka** e rotinas automatizadas de arquivamento de tarefas.

---

## Tecnologias Utilizadas

- **Linguagem & Framework:** Java 21, Spring Boot (WebMVC, Data JPA, Validation, WebSocket, Security, Kafka)
- **Segurança & Autenticação:** Spring Security + Auth0 Java JWT (`java-jwt`)
- **Mensageria & Eventos:** Apache Kafka (`spring-kafka`)
- **Comunicação em Tempo Real:** WebSockets (STOMP + SockJS)
- **Banco de Dados Relacional:** PostgreSQL 16
- **Migração de Banco de Dados:** Flyway Migration (8 scripts versionados em `db/migration`)
- **Documentação da API:** Springdoc OpenAPI / Swagger UI
- **Containerização:** Docker (Multi-stage build) & Docker Compose
- **Utilitários:** Lombok

---

## Arquitetura e Fluxo de Funcionamento

```
                         +-----------------------+
                         |      auth-server      |
                         +-----------+-----------+
                                     |
                          Publica evento user-created
                                     v
                           +-------------------+
                           |   Apache Kafka    |
                           +---------+---------+
                                     |
                          Consome tópico user-created-topic
                                     v
+-------------------+      +-------------------+      +-------------------+
|  Cliente Frontend | ---> |    Kanban API     | ---> |   PostgreSQL 16   |
| (React/Vue/STOMP) | <--- |   (Spring Boot)   |      |   (pg-aprendizado)|
+-------------------+      +---------+---------+      +-------------------+
     (WebSockets)                    |
     Notificações em tempo real <----+
```

### Principais Componentes:
1. **Sincronização de Usuários (Kafka Consumer):** O microserviço consome eventos do tópico `user-created-topic` através do `UserConsumer` e armazena os dados dos usuários na tabela espelho local `users_local`. Isso permite associar responsáveis (*assignees*) às tarefas sem acoplamento direto via HTTP com o servidor de autenticação.
2. **Segurança & Controle de Acesso (RBAC):** Os tokens JWT emitidos pelo `auth-server` são validados via filtro customizado `SecurityFilter`. As rotas de criação e remoção de tarefas exigem a role `ROLE_ADMIN`.
3. **Atualizações em Tempo Real (WebSockets STOMP):** Notificações automáticas via WebSocket no endpoint `/ws` informam os clientes conectados sempre que uma tarefa é criada, tem seu status alterado ou é arquivada.
4. **Arquivamento Automático (Cron Job Scheduled):** A rotina agendada `archiveOldCanceledTasks` roda periodicamente para identificar tarefas canceladas há mais de determinado período e marca o atributo `archived = true`, disparando uma notificação WebSocket para atualização do front-end.

---

## Endpoints da API

### Gestão de Tarefas (`/v1/task`)

| Método | Rota | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/v1/task/create` | Cria uma nova tarefa | `ROLE_ADMIN` |
| `GET` | `/v1/task` | Retorna todas as tarefas ativas (não arquivadas) | Autenticado |
| `GET` | `/v1/task/paged` | Listagem paginada com filtros (status, busca por texto, intervalo de datas) | Autenticado |
| `GET` | `/v1/task/count` | Retorna o total de tarefas agrupadas por status | Autenticado |
| `PATCH` | `/v1/task/{taskId}/status` | Altera o status de uma tarefa (`OPEN`, `IN_PROGRESS`, `UNDER_REVIEW`, `DONE`, `CANCELED`) | Autenticado (Assignee ou Reporter) |
| `GET` | `/v1/task/metrics` | Retorna métricas consolidadas de tarefas por mês no ano corrente | Autenticado |

### Histórico de Tarefas (`/v1/tasks-histories`)

| Método | Rota | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `GET` | `/v1/tasks-histories/{taskId}` | Retorna todo o histórico de alterações de status de uma tarefa específica | Autenticado |

---

## Comunicação em Tempo Real (WebSockets / STOMP)

- **Endpoint de Conexão:** `/ws` (Suporta SockJS e autenticação por cabeçalho `Authorization: Bearer <token>` ou parâmetro `token`).
- **Tópicos de Transmissão (Subscribe):**
  - `/topic/task-created` – Notifica quando uma nova tarefa é criada.
  - `/topic/task-status-changed` – Notifica alterações no status de uma tarefa.
  - `/topic/tasks-archived` – Notifica a lista de IDs de tarefas que foram arquivadas.

---

## Variáveis de Ambiente

A aplicação aceita as seguintes variáveis de ambiente (definidas nos perfis `dev` ou `prod`):

| Variável | Descrição | Exemplo / Valor Padrão |
| :--- | :--- | :--- |
| `APRENDIZADO_DB_URL` / `DEV_DB_URL` | URL de conexão JDBC com o PostgreSQL | `jdbc:postgresql://db:5432/kanban_db` |
| `APRENDIZADO_DB_USERNAME` / `DEV_DB_USERNAME` | Usuário do banco de dados | `postgres` |
| `APRENDIZADO_DB_PASSWORD` / `DEV_DB_PASSWORD` | Senha do banco de dados | `postgres` |
| `AUTH_JWT_SECRET` | Chave secreta HMAC256 para validação dos tokens JWT | `sua-chave-secreta-jwt` |
| `API_SECURITY_TOKEN_ISSUER` | Emissor esperado do token JWT | `auth-server-api` |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para requisições CORS (separadas por vírgula) | `http://localhost:3000,http://localhost:5173` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Endereço dos brokers do Apache Kafka | `kafka:29092` ou `localhost:9092` |

---

## Como Executar o Projeto

### Pré-requisitos
- [Java 21 JDK](https://adoptium.net/)
- [Docker & Docker Compose](https://www.docker.com/)
- Servidor de Autenticação (`auth-server`) e broker Kafka ativos na rede Docker (se for utilizar a stack completa)

---

### Opção 1: Subindo com Docker Compose (Stack Recomendada)

O arquivo `docker-compose.yml` pré-configura a API e o banco de dados PostgreSQL conectados à rede externa `auth-server-network`.

1. Defina as variáveis de ambiente necessárias no seu shell ou arquivo `.env`.
2. Suba os containers:
   ```bash
   docker-compose up -d --build
   ```
3. A API estará disponível em `http://localhost:8082` e o PostgreSQL exposto na porta `5434`.

---

### Opção 2: Execução Local em Modo de Desenvolvimento

1. Suba apenas o banco de dados PostgreSQL via Docker:
   ```bash
   docker-compose up -d db
   ```
2. Certifique-se de definir as variáveis para o perfil `dev` em seu ambiente.
3. Inicie a aplicação via Maven Wrapper:

   - **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```
   - **Windows:**
     ```cmd
     mvnw.cmd spring-boot:run
     ```

---

## Documentação Interativa da API (Swagger / OpenAPI)

Com a aplicação em execução, a documentação Swagger pode ser acessada em:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (ou porta `8082` se executado via Docker Compose)
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

