# DevTrack AI

DevTrack AI is a project and issue tracker built with React, Spring Boot, PostgreSQL, and an optional FastAPI issue suggestion service. The backend owns authentication and application data. The AI service validates the user's signed token and returns suggestions for review; it never writes to the project database.

## Features

- Account registration and login with BCrypt password hashing and signed JWTs.
- Project and issue create, read, update, and delete workflows with project owner checks.
- Issue priority and status, due dates, search, and client-side filters.
- Per-project activity history for project and issue changes.
- In-app notifications for project changes by other members, with unread counts and read state.
- Optional AI generated issue suggestions. Users explicitly add each suggestion.
- Docker Compose setup for PostgreSQL, backend, AI service, and frontend.

## Start the full stack

1. Copy `.env.example` to `.env`.
2. Set `POSTGRES_PASSWORD` and `DB_PASSWORD` to the same local password.
3. Set `JWT_SECRET` to a private Base64 value with at least 32 random bytes. PowerShell can generate one:

   ```powershell
   [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   ```

4. (Optional) Set `OPENAI_API_KEY` to enable AI issue suggestions. Without it, the rest of the tracker works and the suggestion endpoint reports that AI is unavailable.
5. Start services from the project root:

   ```powershell
   docker compose up --build
   ```

Open `http://localhost:3000`. The frontend's Nginx server proxies `/api` requests to the backend. PostgreSQL data is stored in the named `devtrack-postgres-data` volume. To stop services, press Ctrl+C; use `docker compose down` to stop and remove containers while retaining database data.

## Local development

Start PostgreSQL with `docker compose up -d postgres`, then run the backend from `backend/` with JDK 21 and Maven. Export `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in the Maven process environment; Maven does not automatically load the root `.env` file. Start the UI from `frontend/` with `npm ci` and `npm run dev`. Set `VITE_API_URL=http://localhost:8080` in `frontend/.env.local` for direct backend access.

The AI service can run with Python 3.11+ from `ai-service/` after installing `requirements.txt`. Set the same Base64 `JWT_SECRET` and optionally `OPENAI_API_KEY`; then run `uvicorn app.main:app --reload --port 8000`. Set backend `AI_SERVICE_URL=http://localhost:8000` when running the Java service outside Compose.

## API and documentation

- Backend API: `http://localhost:8080`; health: `/api/health` and `/actuator/health`.
- AI health: `http://localhost:8000/api/ai/health` inside the Compose network.
- Backend setup and endpoint notes: [backend/README.md](backend/README.md).
- Service boundaries and current implementation notes: [docs/architecture.md](docs/architecture.md).

## Current limitations

Project membership supports already-registered users; email invitations and account recovery are not implemented. In-app notifications are available; email/push delivery and preferences are not implemented. Document ingestion and retrieval are not implemented. AI suggestions require an OpenAI API key and an available provider connection.
