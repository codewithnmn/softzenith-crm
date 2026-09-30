# SoftZenith CRM

Multi-tenant CRM platform. First tenant: **Western World Visa Services** (study-abroad / visa consultancy), onboarded through the platform onboarding flow.

- `backend/`: Java 21, Spring Boot 4, PostgreSQL, Flyway, Spring Modulith. REST API.
- `frontend/`: Next.js 16 + Tailwind v4 + shadcn/ui (role-based workspaces), typed API client generated from the backend's OpenAPI spec.

Agent/developer guide: [CLAUDE.md](CLAUDE.md). Current status: [HANDOFF.md](HANDOFF.md).
Flowcharts, use-case and state diagrams of every flow: [docs/flows.md](docs/flows.md).

## What works (PRD Phase 1)

- Public enquiry form per tenant: `http://localhost:3000/enquiry/<tenant-slug>` → creates a lead (repeat enquiries attach to the open lead).
- On a new lead: welcome **email** + welcome **WhatsApp (demo: logged, not sent)** to the enquirer, and an alert **email** to
  every staff member whose role has the "new-lead alert" permission (Admin + Receptionist by default).
- Staff sign in (phone), see all leads, add walk-in/phone leads. Admin and Receptionist assign leads to counsellors
  (counsellor gets an email). Admin changes status (New / Contacted / Closed with reason / reopen).
- Admin **dashboard** (`/dashboard`): leads received per period, by status, source and counsellor, open & unassigned.
- Admin onboards staff (counsellor, branch manager, receptionist, admin) with basic employee records, and manages branches.

## Local setup

1. **PostgreSQL 16+** on `localhost:5432`, database `crm` owned by `crm_app`/`crm_app`:
   ```sql
   create role crm_app login password 'crm_app';
   create database crm owner crm_app;
   ```
2. **Mailpit** (local mail catcher, https://mailpit.axllent.org): run `mailpit`. SMTP on 1025, inbox at http://localhost:8025.
3. **Backend** (port 8081, seeds a neutral demo tenant `demo` (Demo Visas) with demo staff):
   ```sh
   cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   Swagger: http://localhost:8081/swagger-ui.html
4. **Frontend** (port 3000):
   ```sh
   cd frontend && npm install && npm run dev
   ```
5. Open http://localhost:3000. In dev, sign in with just a phone number (no OTP):
   `9000000001` Admin · `9000000002` Branch Manager · `9000000003` Counsellor · `9000000004` Receptionist.
   Public form: http://localhost:3000/enquiry/demo

## Configuration (environment variables)

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | local `crm` | Postgres |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | `localhost:1025` (Mailpit) | SMTP for real email (Resend, SES, Brevo, Gmail...) |
| `MAIL_FROM` | `no-reply@softzenith.local` | From address; display name is the tenant's |
| `FRONTEND_URL` | `http://localhost:3000` | Links in emails |
| `SUPABASE_URL` | placeholder | Supabase project for real phone-OTP sign-in |
| `PORT` | 8081 | API port |
| frontend `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY` | empty = dev login | see `frontend/.env.example` |

## Authentication

Production: staff sign in with **phone OTP through Supabase Auth**; the API only verifies the token. Tenant, role and
permissions come from our own tables (an admin adds staff by phone; the first sign-in links them).
Development (`dev` profile): `POST /api/v1/dev/login {phone}` issues a token without OTP. Never enable it in production.

## Tests

```sh
cd backend && ./mvnw test           # embedded PostgreSQL + GreenMail; no Docker
cd frontend && npx tsc --noEmit && npm run lint && npm run build
```

## Onboarding another tenant

No code changes: create the tenant via `TenantOnboardingService` (tenant + default roles), set its `settings`
(lead-number prefix, enquiry-form options, sender name, welcome on/off), add branches and an admin. Its enquiry form is
immediately live at `/enquiry/<slug>`.
