# AGENTS.md

## Build & Run

```powershell
# System JAVA_HOME is Java 8 — override to JDK 21
$env:JAVA_HOME = "D:\javaApps\jdks\jdk21"

# Compile all modules
mvn compile -q

# Compile a single module + dependencies
mvn compile -pl lin-common/lin-common-ratelimiter -am -q

# Run the app
mvn spring-boot:run -pl lin-main

# Run tests (only lin-main has tests; common modules have none)
mvn test -pl lin-main
```

No Maven wrapper, no lint/format/typecheck commands.

## Architecture

- **Spring Boot 3.4.5, Java 21**, base package `com.lin`
- Multi-module Maven: `lin-main` (application) + `lin-common` (library aggregator)
- `lin-common` submodules: `lin-common-core`, `lin-common-jackson`, `lin-common-ratelimiter`

## Conventions

- **Controller return type is mandatory `Result<T>`** (`com.lin.common.result.Result`) — never return raw objects
- **Business errors** → `throw new BusinessException("message")` — handled by `GlobalExceptionHandler`
- **Auto-configuration**: new modules use `@AutoConfiguration` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Spring Boot 3.x style), not `spring.factories`
- **New dependency**: add `<dependencyManagement>` entry in root `pom.xml`; new submodule: add `<module>` in `lin-common/pom.xml`
- **Design docs**: `.md` files live alongside source code in `src/main/java/`
- **Lombok `@Slf4j`** is used for logging; `spring-boot-starter-web` brings in AOP transitively
- **`lin-common-ratelimiter`** requires a `RedissonClient` bean (Redis) — module auto-activates via `@ConditionalOnBean`

## Key Files

| File | Role |
|------|------|
| `lin-main/src/main/java/com/lin/LinMainApplication.java` | App entry point |
| `lin-main/src/main/resources/application.yml` | Config (port 8080) |
| `lin-common/lin-common-core/.../result/Result.java` | Unified API response wrapper |
| `lin-common/lin-common-core/.../exception/GlobalExceptionHandler.java` | Global `@RestControllerAdvice` |
| `lin-common/lin-common-core/.../exception/BusinessException.java` | Throwable business error |
