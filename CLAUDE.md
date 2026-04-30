# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build all modules
mvn clean package -DskipTests

# Run tests
mvn test
mvn test -pl lin-main -Dtest=ClassName          # single test class
mvn test -pl lin-main -Dtest=ClassName#method    # single test method

# Run the app (starts on port 8080)
mvn spring-boot:run -pl lin-main
```

Java 21, Spring Boot 3.4.5, Maven multi-module.

## Architecture

```
Increment (root pom, version 1.0-SNAPSHOT)
├── lin-main           → Spring Boot app entry point, wires all modules together
├── lin-common (pom)   → shared libraries (auto-config, no manual scanning needed)
│   ├── lin-common-core        → Result<T>, ResultCode, BusinessException, GlobalExceptionHandler, RedisKeyPrefixProvider
│   ├── lin-common-jackson     → Jackson auto-config, BigNumberSerializer, CustomDateDeserializer, JsonUtils, @JsonPattern
│   ├── lin-common-ratelimiter → @RateLimiter AOP with Redisson (IP/CLUSTER/DEFAULT modes, SpEL key support)
│   ├── lin-common-redis       → RedisUtils (static), RedisConfig (CompositeCodec + KeyPrefixHandler), RedisKeyConflictChecker
│   ├── lin-common-security    → JWT auth: TokenAuthenticationFilter + SecurityInterceptor + @RequirePermission AOP
│   └── lin-common-crypto      → @EncryptResponse + AES-256-GCM ResponseBodyAdvice, session key management
└── lin-modules (pom)  → business modules
    ├── lin-module-admin       → admin controllers (Auth, Order, Test), Manager/Service pattern
    └── lin-module-api         → public API controllers
```

## Key Patterns

- **Unified response**: All controllers return `Result<T>` with code + msg + data. Success → `ResultCode.SUCCESS` (200). Never throw strings.
- **Business exceptions**: Throw `BusinessException(String msg)` or `BusinessException(ResultCode code)`. Caught by `GlobalExceptionHandler` → `Result.error(...)`.
- **Auto-configuration**: Config classes use `@AutoConfiguration` (not `@Configuration`) so they're picked up automatically by any module that depends on them. No `@ComponentScan` or `spring.factories` needed. Enabled/disabled via `@ConditionalOnBean` or `@ConditionalOnProperty`.
- **Jackson**: `BigNumberSerializer` converts Long/BigInteger/BigDecimal to strings when outside JS safe integer range (prevents frontend precision loss). `CustomDateDeserializer` auto-detects date formats via Hutool. All wired in `JacksonConfig`.
- **Rate limiting**: Annotate any controller method with `@RateLimiter(time=60, count=10, limitType=LimitType.IP)`. Requires Redisson on classpath. AOP aspect auto-registers via `RateLimiterConfig` when `RedissonClient` bean exists.
- **Security (JWT)**: Three-layer design. `TokenAuthenticationFilter` (OncePerRequestFilter) parses Bearer token → `UserContext` (ThreadLocal). `SecurityInterceptor` (HandlerInterceptor) decides: whitelist match, `@Anonymous` on class/method, or authenticated user → pass; otherwise 401. `@RequirePermission("user:write")` (AOP) checks `UserContext.getPermissions()`. Config via `lin.security.*` properties; defaults enabled with 7200s token expiry.
- **Redis utilities**: `RedisUtils` is a static facade — injects `RedissonClient` via `RedisClientInitializer` on startup. Covers String/List/Set/Map/Atomic/RLimiter operations. Key encoding: `StringCodec` for keys, `TypedJsonJacksonCodec` for values (ISO-8601 LocalDateTime).
- **Redis Key prefix management**: Each module defines a `PREFIX` constant and implements `RedisKeyPrefixProvider` (defined in core). `RedisKeyConflictChecker` scans all implementations at startup and logs errors if prefixes collide — prevents runtime key overwrites between modules.
- **Manager pattern** (lin-module-admin): `OrderManager` orchestrates multiple `@Service` beans (UserService → InventoryService → OrderService → PaymentService). Manager handles sequencing and transaction boundaries; services are single-responsibility.
- **Response encryption**: `@EncryptResponse` on Controller class/method → `EncryptResponseBodyAdvice` (ResponseBodyAdvice) encrypts `Result.data` using AES-256-GCM. Session key (base64) returned during login as `encryptKey` field. Response header `X-Encrypted: true` signals encryption. Frontend decrypts with Web Crypto API using the session key.

## Dependencies

- **Hutool 5.8.27** — general-purpose Java utils (date, string, collection helpers)
- **Knife4j 4.5.0** — Swagger UI at `/doc.html` (Spring Boot 3 jakarta variant)
- **Redisson 3.40.0** — Redis-based distributed rate limiter (optional; ratelimiter auto-disables without it)
- **Lombok** — `@Data`, `@Slf4j`, `@AllArgsConstructor` are used throughout

---

## Behavioral Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

### 3. Surgical Changes

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- Remove imports/variables/functions that YOUR changes made unused.

### 4. Goal-Driven Execution

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
```
