# SENG 403 Question Pool

A small site where students write multiple-choice questions for each chapter, answer each other's questions,
and discuss them. The instructor approves questions and sees who took part. Midterm questions come from this pool.

- **Backend:** Spring Boot 4 (Java 21), PostgreSQL, Flyway. Code in `backend/`.
- **Frontend:** Vue 3 + Vite, mobile first. Code in `frontend/`. It is built into the Spring Boot jar.
- **AI:** OpenRouter (optional). Checks new questions, writes explanations, answers in discussion threads.
- **Chapter texts:** `chapters/week<N>.en.md` and `chapters/week<N>.tr.md`.

## Main rules

- **Login:** a student writes their school e-mail and gets a single-use link (15 minutes). Only addresses on the
  class list get a link. There are no passwords.
- **Nicknames:** students see each other's nickname (for example `CalmOtter42`). Only the instructor sees real names.
- **Attempt first:** the correct answer, the reasons, the AI explanation and the discussion stay hidden until the
  student answers the question. Only the first answer counts.
- **Question life:** waiting → (automatic AI check gives feedback) → the instructor approves, sends back, or rejects.
  The author can change a question until it is approved or rejected.
- **Limit:** 2 questions per student per chapter (`QUESTIONS_PER_CHAPTER`). Rejected questions do not count.
- **Deleting people:** the instructor can delete a teacher or a student. This is a lazy delete: the account row
  stays, so questions, answers, comments and flags keep their author and the reports stay correct. The person can
  no longer sign in and an open session ends at once. A teacher cannot delete their own account. Adding the same
  e-mail again (or importing the class list again) brings the person back.

## Run on the server

```bash
cp .env.example .env      # then fill it in
docker compose up -d --build
```

The app listens on `127.0.0.1:8080`. Put your reverse proxy with HTTPS in front of it and set `APP_BASE_URL` to the
public address.

Once the image is pushed to Docker Hub as `armikom/question-pool` (see *Docker image* below), a server needs only
`docker-compose.yml` and `.env`:

```bash
docker compose pull && docker compose up -d
```

First steps after the start:

1. Open the site and sign in with `INSTRUCTOR_EMAIL`.
2. **Students** → paste the class list (`e-mail, full name, student number`, one per line) → Import.
3. **Chapter texts** → check the texts. On the first start they are loaded from `chapters/`. After that, change them
   on this screen (files are read only while a chapter text is still empty).
4. Send one test login link to a student address, to check that the school mail server accepts your mail.

Backup of the database:

```bash
docker compose exec db pg_dump -U questionpool questionpool > backup.sql
```

## Settings (`.env`)

| Name | Meaning |
|---|---|
| `DB_PASSWORD` | Password of the database |
| `APP_BASE_URL` | Public address; login links start with it |
| `SECURE_COOKIES` | `true` when the site uses HTTPS |
| `INSTRUCTOR_EMAIL`, `INSTRUCTOR_NAME` | The instructor account, created on the first start |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM` | Mail account that sends login links. With an empty host, links are written to the app log |
| `EWS_URL` | For Outlook-on-the-web (on-premises Exchange) accounts whose SMTP ports are closed, e.g. `https://mail.example.edu/EWS/Exchange.asmx`. When set, mail goes through EWS (NTLM) with `SMTP_USER`/`SMTP_PASSWORD`; the sender is always that mailbox |
| `OPENROUTER_API_KEY`, `OPENROUTER_MODEL` | AI. With an empty key the site works without AI features |
| `QUESTIONS_PER_CHAPTER`, `ASK_AI_PER_DAY` | Limits for students |

## Develop on your computer

You need Java 21+, Node 22+ and a PostgreSQL database `questionpool` (user and password `questionpool`).

```bash
cd backend && INSTRUCTOR_EMAIL=you@example.com ./mvnw spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```

Open http://localhost:5173. Without SMTP settings the login link is printed in the backend log.

## Exam builder

**Exams** (instructor menu) → set how many questions to take from each chapter and how many forms (A, B, …) →
**Draw the questions**. Only approved questions are used; questions with an open problem report are skipped unless
you untick the box. On the exam page you can swap a question for another one from the same chapter, remove it, or
edit its text.

Every form has the same questions. The order of the questions inside a chapter and the order of the options are
different in each form. **Form A / Key A …** open print pages: use the browser's print window and choose
"Save as PDF". If you change the exam after printing, print every form and key again.

## Docker image

The image name is `armikom/question-pool`. `docker compose build` tags it locally, `docker compose push app` publishes
it (log in first with `docker login` as the Docker Hub account `armikom`).

```bash
docker compose build
docker compose push app
```

The image holds the jar and the chapter texts from `chapters/` (used on the first start for chapters that have no text
yet), so running it directly needs only `.env`:

```bash
docker run --rm -p 127.0.0.1:8080:8080 --env-file .env armikom/question-pool:latest
```

## License

MIT — see [LICENSE](LICENSE).
