# DevTrack AI Backend

Spring Boot 3.5 backend built with Java 21 and Maven.

## Run locally

Start PostgreSQL from the project root with `docker compose up -d postgres`. Configure the environment variables listed below, then run from this directory:

```powershell
$env:DB_PASSWORD = "change-me-locally"
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
mvn spring-boot:run
```

The application listens on port 8080 by default. `JWT_SECRET` is required and must be a Base64-encoded value containing at least 32 random bytes. The command above generates one for the current PowerShell session. Store a persistent value in your ignored `.env` file or a secure environment manager; do not commit it. When running Maven, the values must be exported to that terminal's environment: copying `.env.example` to `.env` alone does not load variables into a regular Maven process. The placeholder in `.env.example` is intentionally blank. Other settings are `JWT_EXPIRATION` (ISO-8601 duration, default `PT1H`), `DB_URL` (default `jdbc:postgresql://localhost:5432/devtrack`), `DB_USERNAME` (default `devtrack`), `DB_PASSWORD` (must match the local Compose password), and `SERVER_PORT` (default `8080`).

Flyway applies versioned SQL migrations at startup. Hibernate validates mappings against the resulting schema and does not create or alter tables.

## Endpoints

- `GET /api/health` — returns an application status and timestamp.
- `GET /actuator/health` — Spring Boot Actuator health status, without component details.
- `POST /api/auth/register` — creates an account and returns a bearer access token (HTTP 201).
- `POST /api/auth/login` — validates credentials and returns a bearer access token (HTTP 200).

All other requests require `Authorization: Bearer <token>`. The API is stateless. Registration accepts a name, email and password (8–72 characters); duplicate emails return HTTP 409, invalid requests HTTP 400, and invalid credentials HTTP 401. Errors use a JSON body with status, message, request path, timestamp and field errors where applicable. Passwords are stored as BCrypt hashes and are never included in responses.

Project endpoints require a bearer token. `POST /api/projects` creates a project; `GET /api/projects` returns projects owned by or shared with the authenticated user (`page`, `size`, `sort`); `GET /api/projects/{id}` retrieves one accessible project; `PUT /api/projects/{id}` updates its name and description; and `DELETE /api/projects/{id}` removes it. Projects default to `ACTIVE`, and missing or inaccessible project IDs both return 404.

Issue endpoints are nested under an accessible project: `POST /api/projects/{projectId}/issues`, paginated `GET /api/projects/{projectId}/issues`, `GET /api/projects/{projectId}/issues/{issueId}`, `PUT /api/projects/{projectId}/issues/{issueId}`, and `DELETE /api/projects/{projectId}/issues/{issueId}`. Issues use `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` priority and `OPEN`, `IN_PROGRESS`, `RESOLVED`, or `CLOSED` status. The supplied project ID must match the issue's actual project; read and edit permissions depend on project membership role.

Issue listing supports server-side `status`, `priority`, and `search` query parameters together with Spring pagination (`page`, `size`, `sort`).

Project owners can archive or reactivate projects with `PATCH /api/projects/{id}/status` and a body such as `{"status":"ARCHIVED"}`. Projects can be shared with registered accounts using `GET`, `POST`, `PUT /{userId}`, and `DELETE /{userId}` under `/api/projects/{projectId}/members`. Roles are `ADMIN`, `MEMBER`, and `VIEWER`; viewers have read access, while admins and members can modify issues. Only the owner can change project details or manage membership. Non-member access returns 404.

`GET /api/projects/{projectId}/activity` returns a paginated project activity feed. `GET /api/notifications` and `GET /api/notifications/unread-count` return notifications for the current user; `PATCH /api/notifications/{id}/read` marks one as read. Project and issue changes notify other project members.

`POST /api/projects/{projectId}/ai/suggest-issues` forwards authorized project context to the optional FastAPI service. Configure `AI_SERVICE_URL`, and set the same `JWT_SECRET` in both services. The AI endpoint needs `OPENAI_API_KEY`; its absence does not affect normal tracker workflows.

## Verify

Run `mvn test` to execute the health, authentication, project, and issue API tests. They use embedded H2 and run all Flyway migrations without contacting PostgreSQL. Build a runnable jar with `mvn package`. For local application startup, start PostgreSQL and configure `DB_PASSWORD` and `JWT_SECRET` as described above, then run `mvn spring-boot:run`; Flyway applies new migrations automatically and existing database volumes are preserved.
