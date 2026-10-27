# DevTrack AI frontend

React 19 and TypeScript single-page app. It provides registration, login, dashboard, project CRUD and archiving, project member management, issue CRUD and filtering, activity history, and human-reviewed AI issue suggestions.

## Run locally

```powershell
npm ci
$env:VITE_API_URL = "http://localhost:8080"
npm run dev
```

The backend must be running at the configured API URL. For a deployed build behind the included Nginx config, leave `VITE_API_URL` empty so the browser uses the same origin and Nginx proxies `/api` requests to the backend.

## Commands

- `npm run dev` starts Vite.
- `npm run build` creates the production frontend bundle.
- `npm run lint` runs ESLint.
- `npm run preview` serves the production bundle locally.
