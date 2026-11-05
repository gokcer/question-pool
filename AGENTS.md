# AGENTS.md

Guidance for AI coding agents working in this repository.

- **Product overview:** `README.md`
- **Live deployment (server, containers, Cloudflare tunnel, env, deploy/backup steps):** `.deployment`

## What this is

SENG 403 Question Pool — students write multiple-choice questions per chapter, answer each other's
questions and discuss them; the instructor approves questions and sees who took part.

## Stack and layout

- **Backend:** Spring Boot 4 (Java 21), PostgreSQL, Flyway — `backend/`
- **Frontend:** Vue 3 + Vite, mobile first — `frontend/` (built into the Spring Boot jar)
- **AI:** OpenRouter, optional — `backend/.../AiService.java`
- **Chapter texts:** `chapters/week<N>.en.md` and `chapters/week<N>.tr.md`
- **Deployment notes:** `.deployment`

## Build, run, test

```bash
# backend (needs Java 21+; see README for DB + env)
cd backend && INSTRUCTOR_EMAIL=you@example.com ./mvnw spring-boot:run
cd backend && ./mvnw -q compile        # quick compile check

# frontend
cd frontend && npm install && npm run dev
```

There are no automated Java or frontend tests in this repo. Validate changes by compiling the
backend (`./mvnw -q compile`) and, when relevant, running the app locally.

## Conventions

- Match the existing code style (tabs in Java, 2-space indent + single quotes in Vue/JS).
- UI text is bilingual (English + Turkish), the same style already used in the views.
- The backend builds the frontend into its jar; `/api` requires an authenticated session
  (see `WebConfig`, `AuthService`). Keep configuration in `.env` / `application.properties`.

## Deployment

The site runs on the `armikom-vm-external` server under `/home/armikom/dockers/question-pool`
behind a Cloudflare Tunnel. **All deployment details — SSH, containers, networking, env values,
deploy/backup commands — are documented in `.deployment`.** Read it before touching anything
deployment related. Secrets (`DB_PASSWORD`, `SMTP_PASSWORD`, `OPENROUTER_API_KEY`, `TUNNEL_TOKEN`)
live only in the server `.env` and must never be committed.
