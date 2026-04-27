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
├── lin-main           → Spring Boot app, controllers, DTOs, config
└── lin-common (pom)   → shared libraries
    ├── lin-common-core        → Result<T>, ResultCode, BusinessException, GlobalExceptionHandler
    ├── lin-common-jackson     → Jackson auto-config, BigNumberSerializer, CustomDateDeserializer, JsonUtils, @JsonPattern
    └── lin-common-ratelimiter → @RateLimiter AOP with Redisson (IP/CLUSTER/DEFAULT modes, SpEL key support)
```

## Key Patterns

- **Unified response**: All controllers return `Result<T>` with code + msg + data. Success → `ResultCode.SUCCESS` (200). Never throw strings.
- **Business exceptions**: Throw `BusinessException(String msg)` or `BusinessException(ResultCode code)`. Caught by `GlobalExceptionHandler` → `Result.error(...)`.
- **Auto-configuration**: Config classes use `@AutoConfiguration` (not `@Configuration`) so they're picked up automatically by any module that depends on them. No `@ComponentScan` or `spring.factories` needed.
- **Jackson**: `BigNumberSerializer` converts Long/BigInteger/BigDecimal to strings when outside JS safe integer range (prevents frontend precision loss). `CustomDateDeserializer` auto-detects date formats via Hutool. All wired in `JacksonConfig`.
- **Rate limiting**: Annotate any controller method with `@RateLimiter(time=60, count=10, limitType=LimitType.IP)`. Requires Redisson on classpath. AOP aspect auto-registers via `RateLimiterConfig` when `RedissonClient` bean exists.

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
