## Suggested specific improvements
### 1. Simplify controller async handling
- Remove throws ExecutionException, InterruptedException, TimeoutException from controller methods.
- Consider making controller methods synchronous and moving timeouts to the service or client-side, unless you have a specific reason for CompletableFuture at the web layer.
- For logout, perform the logout synchronously or at least wait for the async task and return a meaningful status/error if it fails.

### 2. Refine events and domain modeling

- Move UserEvent, UserCreatedEvent, UserLoggedInEvent, UserLoggedOutEvent out of AuthenticationService into com.pluto.pluto_interview.event.
- Keep events immutable and well-documented; treat them as part of the domain language.


### 3. Configuration & environment profiles

- Use application-dev.yaml for dev-only settings (ddl-auto: update, localhost hosts, etc.) and make application.yaml production-safe by default.
- Externalize CORS allowed origins, security headers, and rate-limiting policies to properties (per profile) rather than hard-coding them in SecurityConfig.
- Ensure secrets (jwt.secret, DASHSCOPE_API_KEY, pgvector credentials) are only referenced via env vars and document this in a README.

### 4. Observability & operations

- Add Micrometer + Prometheus (or another registry) and define basic business metrics (login attempts, refresh operations, token failures).
- Add structured logging with correlation/trace IDs where applicable.
- Consider OpenTelemetry instrumentation for key services if this is meant to mimic production.


### 5. Testing and QA

- Add at least a couple of slice/integration tests (e.g., @SpringBootTest or @WebMvcTest for controllers) to exercise wiring and security config.
- Add tests for edge cases in token refresh, lock timeouts, and error handling flows in GlobalExceptionHandler.


### 6. Code hygiene

- Remove commented-out old implementations (e.g., SimpleRedisLock pieces) or move them behind feature flags if they’re intended as alternatives.
- Centralize key prefixes (TOKEN_KEY_PREFIX, REFRESH_TOKEN_KEY_PREFIX, LOCK_NAME_PREFIX) in one place, and ensure all consumers use them.