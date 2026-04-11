# Pluto Interview Backend Refactor Guideline

## Goals and constraints
- Make the service secure, reliable, and production-ready while keeping the scope feasible for a single developer.
- Preserve current functionality and API behaviour unless explicitly improved.
- Prefer incremental, low-risk changes with measurable outcomes.

## Quick definitions (for shared understanding)
- **CORS (Cross-Origin Resource Sharing)**: browser rules that control which web origins can call your API. Too-permissive CORS can enable data leakage from a malicious site.
- **CSRF (Cross-Site Request Forgery)**: an attack where a logged-in browser is tricked into sending requests to your API. CSRF is relevant for cookie-based auth; for bearer-token auth it is less critical but still worth being explicit.
- **HSTS (HTTP Strict Transport Security)**: a browser policy that forces HTTPS and prevents downgrade to HTTP.
- **JWT (JSON Web Token)**: a signed token that proves authentication; if long‑lived or not revoked, a leaked token enables access until expiry.
- **SSE (Server-Sent Events)**: a one-way streaming HTTP response. It needs timeouts, backpressure awareness, and clean cancellation.

## Priority checklist (single‑developer friendly)
### P0 — Immediate security and data‑leak risks
- **Remove hard-coded credentials and hosts** (e.g. PgVector username/password in `src/main/java/com/pluto/pluto_interview/config/VectorStoreConfig.java`).
  - Move to environment variables or Spring `@ConfigurationProperties` and ensure secrets are not committed.
- **Replace permissive CORS configuration with environment‑specific allowlists** in `src/main/java/com/pluto/pluto_interview/config/SecurityConfig.java`.
  - Define a strict list for production and a separate list for local development.
- **Make the JWT revocation model explicit**.
  - Today the JWT filter checks Redis presence; if Redis is down, auth can fail or behave inconsistently. Decide whether Redis is required for all auth (stateful JWT) or only for refresh tokens.
- **Validate JWT audience/issuer/algorithms** in your `JwtService` implementation to avoid algorithm‑confusion attacks.
  - Ensure accepted algorithms are strict and match your signing keys.

### P1 — Authentication and session hardening
- **Rotate refresh tokens** and persist their fingerprint/ID rather than the raw token.
  - This reduces token replay risk if Redis is compromised.
- **Shorten access token TTL and extend refresh TTL with rotation**, and log abnormal refresh patterns.
- **Limit login attempts per account and per IP**.
  - You already have a `RateLimitingFilter`; apply it to `/auth/login` and `/auth/register` specifically with tighter limits.
- **Introduce account security signals** (failed login count, recent login IP) in the database for audit and incident response.

### P2 — Data access and persistence safety
- **Switch from `ddl-auto: update` to Flyway migrations** in production.
  - Auto‑update can produce unexpected schema changes and is risky for production.
- **Add database constraints and indices** for user email, session ownership, and message ordering.
- **Implement soft delete** for user data and conversations if legal or product requirements call for retention.

### P3 — LLM safety and reliability
- **Add prompt‑injection guardrails**.
  - Validate or strip user content before it is appended to system prompts, and isolate system instructions from user input.
- **Introduce output validation** for structured responses (e.g. JSON schema validation on AI outputs in `MockInterviewEvaluator`).
  - Reject invalid model outputs and retry with a constrained prompt.
- **Set token/latency budgets** for LLM calls and enforce timeouts and cancellation for SSE streams.

### P4 — Observability and diagnostics
- **Add structured logging with correlation IDs** (request ID, user ID, session ID).
  - This enables tracing issues across asynchronous flows.
- **Add health checks and readiness probes**.
  - Expose DB and Redis connectivity without leaking secrets.
- **Centralise exception handling** to avoid leaking internal errors to clients.
  - You already have `GlobalExceptionHandler`; ensure generic 5xx handling is enabled.

### P5 — Testing and quality gates
- **Add contract tests for auth and SSE endpoints**.
  - Include negative tests for invalid JWTs, expired refresh tokens, and missing auth headers.
- **Add unit tests for security configuration** (CORS, CSRF, public endpoints) and for token rotation logic.
- **Add linting/static analysis** (SpotBugs, ErrorProne, or Sonar).

## Modernisation opportunities (low effort → higher impact)
### Low effort (1–3 days)
- Move secrets and hostnames to environment variables and `@ConfigurationProperties`.
- Harden CORS and explicitly document environment‑specific origins.
- Enforce JWT issuer/audience checks and a fixed signing algorithm.
- Add default timeouts for async flows and SSE (server + client).

### Medium effort (1–2 weeks)
- Refresh token rotation with server‑side tracking and revocation.
- Introduce request correlation IDs and structured JSON logging.
- Add a simple audit trail for auth events.
- Add schema migrations and remove `ddl-auto: update` in production.

### Longer‑term (2–6 weeks)
- Introduce a dedicated auth service or OAuth2/OIDC integration.
- Add a robust prompt safety layer (input filters + output validators + red‑team tests).
- Build a full test suite (integration + performance + security regression).

## Suggested order of execution
1. Secrets and configuration hygiene (remove hard-coded credentials, tighten CORS).
2. JWT validation and refresh token rotation.
3. Database migration discipline (Flyway-only for production).
4. Observability and error handling improvements.
5. LLM safety and output validation.
6. Tests and CI quality gates.

## Known risk hotspots in this codebase (from current context)
- Hard-coded PgVector credentials in `VectorStoreConfig`.
- CSRF disabled and CORS set to a single hard-coded origin; no environment separation.
- JWT filter relies on Redis presence for access token validity (stateful JWT), which needs explicit operational guarantees.
- `ddl-auto: update` enabled by default.
- SSE and async flows with limited cancellation and timeout handling.

## Open questions to refine scope
- Which environments are first‑class targets (local, staging, production)?
- Are you willing to introduce a secrets manager (Vault, AWS/GCP secret stores), or only env vars?
- Is a managed auth provider acceptable, or must JWT remain in-house?
- Are there compliance requirements (GDPR, data retention, audit logs)?

