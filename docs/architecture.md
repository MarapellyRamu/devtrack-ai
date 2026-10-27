# Architecture and implementation status

## Service boundaries

- **Backend (`backend/`)** is the source of truth for users, projects, memberships, issues, access checks, and activity history. Spring Boot uses PostgreSQL, Flyway migrations, and stateless JWT authentication.
- **Frontend (`frontend/`)** is a React/TypeScript single-page app. During local development it talks to the backend directly; in Compose, Nginx proxies `/api` to the backend.
- **AI service (`ai-service/`)** validates the same signed JWT as the backend and returns validated issue suggestions. It has no database credentials and cannot write application records. The backend checks access to the requested project before forwarding project context.
- **Database** stores relational application data. Migrations are append-only. PostgreSQL data persists in the Compose named volume.

Request flow for normal app data is browser → Spring Boot → PostgreSQL. For AI suggestions it is browser → Spring Boot (project access check) → FastAPI (JWT check) → configured OpenAI API. Suggested issues are only persisted after the user chooses to add them.

## Implemented features

- Registration and login, BCrypt passwords, signed one-hour JWTs by default, and protected API routes.
- Project and issue CRUD, pagination, status and priority fields, due dates, search, and filters.
- Project membership for registered users with `ADMIN`, `MEMBER`, and `VIEWER` roles. Project owners manage membership; viewers can read; admins and members can also create, edit, and delete issues. Only the owner can edit or delete the project.
- Project activity history for creation and updates to projects and issues, and issue deletion.
- In-app notifications to other project members for project and issue changes, with user-scoped read state.
- Optional AI issue suggestions, with provider output validation and human review before saving.
- Docker Compose for PostgreSQL, backend, AI service, and frontend.

## Security notes

- Set a private Base64 encoded JWT signing secret (at least 32 random bytes) and a non-placeholder database password before startup.
- The same JWT secret must be available to the backend and AI service so both validate tokens consistently.
- `OPENAI_API_KEY` is optional and must remain server-side. The browser never calls the provider directly.
- Membership invitations currently require the invited user to have registered already; email delivery is not configured.

## Remaining product work

- Email based invitations, password reset, and account recovery.
- Email/push notification delivery and user notification preferences.
- Document ingestion, retrieval, and source citations.
- Broader operational hardening such as deployment secrets, backups, monitoring, and production TLS/reverse proxy configuration.
