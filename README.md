# Kanban API

Esta é uma API RESTful desenvolvida em Java com Spring Boot para o gerenciamento de um quadro Kanban. A aplicação inclui funcionalidades para gerenciamento de tarefas, histórico de movimentações, comunicação em tempo real utilizando WebSockets e arquivemento automatico de tarefas canceldas há um x tempo definido pelo usuário.

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot** (WebMVC, Data JPA, Validation, WebSocket)
- **PostgreSQL 16** (Banco de Dados Relacional)
- **Flyway** (Migração de Banco de Dados)
- **Lombok** (Redução de Boilerplate)
- **Springdoc OpenAPI (Swagger)** (Documentação da API)
- **Docker & Docker Compose** (Containerização)

## Estrutura do Projeto e Funcionalidades

O projeto possui as seguintes entidades e funcionalidades principais:
- **User**: Gerenciamento de usuários.
- **Task**: Tarefas do Kanban, com funcionalidades que incluem título, descrição, status e arquivamento.
- **TaskHistory**: Histórico de alterações e movimentações das tarefas.
- **Arquivamento Automático (Cron Job)**: O sistema realiza uma varredura periódica de forma automática utilizando o método agendado `archiveOldCanceledTasks`, que localiza tarefas canceladas antigas e as arquiva, mantendo a organização do quadro e otimizando o banco de dados.

A comunicação em tempo real via **WebSockets** (STOMP + SockJS) está ativada no endpoint `/ws`, permitindo que aplicações frontend (como React, Vue ou Angular) recebam atualizações das tarefas em tempo real (por exemplo, ao mover cards no Kanban).

## Pré-requisitos

Para rodar o projeto localmente, você precisará ter instalado:
- [Java 21](https://adoptium.net/)
- [Docker e Docker Compose](https://www.docker.com/) (Para rodar o banco de dados ou a stack completa via container)
- *Maven (Opcional, pois o projeto usa o Maven Wrapper `mvnw`)*

## Configuração e Execução

### Opção 1: Utilizando Docker Compose (Stack Completa)

O projeto possui um arquivo `docker-compose.yml` que configura e sobe o banco de dados PostgreSQL e a API. 

1. Certifique-se de configurar as seguintes variáveis de ambiente (via export ou em um arquivo `.env` caso configurado):
   - `APRENDIZADO_DB_DATABASE`
   - `APRENDIZADO_DB_USERNAME`
   - `APRENDIZADO_DB_PASSWORD`
   - `APRENDIZADO_DOCKER_DB_URL` (exemplo: `jdbc:postgresql://db:5432/nome_do_banco`)

2. Suba os containers:
   ```bash
   docker-compose up -d --build
   ```
*(Neste modo, a API estará acessível na porta externa **8081**: `http://localhost:8081`)*

### Opção 2: Executando a API Localmente (Desenvolvimento)

1. Você pode subir apenas o banco de dados através do Docker:
   ```bash
   docker-compose up -d db
   ```
2. Inicie a aplicação via Maven Wrapper:
   ```bash
   # Em sistemas baseados no Unix
   ./mvnw spring-boot:run
   
   # No Windows
   mvnw.cmd spring-boot:run
   ```
*(O Flyway irá rodar automaticamente no momento de inicialização (startup) do Spring para criar e migrar a estrutura do banco de dados baseando-se nos scripts dentro da pasta `db/migration`.)*

## Documentação da API

A documentação interativa da API gerada automaticamente via Swagger (Springdoc OpenAPI) pode ser acessada através das seguintes rotas:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (ou porta `8081` se estiver rodando via `docker-compose api`)
- **Documentação JSON:** `http://localhost:8080/v3/api-docs`

## Controllers da API
- `TaskController`: Manipulação, listagem paginada e gestão do ciclo de vida e status das tarefas.
- `UserController`: Operações envolvendo usuários.
- `TaskHistoryController`: Consulta ao histórico de alterações nas tarefas.
