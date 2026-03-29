# AGENTS.md — Guidance for Agentic Coding Tools

This file provides context, conventions, and commands for AI coding agents working in the `api-oha-benchmarker` repository.

---

## Project Overview

`api-oha-benchmarker` is a **CLI-only Spring Boot 4 application** (no web layer) that orchestrates Docker containers via Testcontainers, runs HTTP load tests using the external `oha` CLI tool, collects CPU/memory metrics via cAdvisor and `docker stats`, and writes a structured results file. It is a pure batch/benchmarking tool — there is no REST API, no embedded server, and no UI.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 (GraalVM CE 25.0.1 recommended) |
| Framework | Spring Boot 4.0.5 (non-web, CommandLineRunner) |
| Build | Apache Maven 3.9.14 via Maven Wrapper (`./mvnw`) |
| Containers | Testcontainers (used in production, not just tests) |
| Load testing | `oha` CLI (external binary, called via `ProcessBuilder`) |
| Metrics | cAdvisor container + `docker container stats` |
| Boilerplate | Lombok (`@Slf4j`, `@RequiredArgsConstructor`, `@Getter`) |
| Validation | Jakarta Bean Validation (`@NotNull`, `@NotEmpty`, etc.) |
| Testing | JUnit 5 (Jupiter) + Spring Boot Test |

---

## Build & Run Commands

```bash
# Compile
./mvnw compile

# Check formatting (enforced on ./mvnw verify)
./mvnw spotless:check

# Fix formatting
./mvnw spotless:apply

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ApiOhaBenchmarkerApplicationTests

# Run a single test method
./mvnw test -Dtest=ApiOhaBenchmarkerApplicationTests#contextLoads

# Package JAR (skipping tests)
./mvnw package -DskipTests

# Clean build
./mvnw clean package

# Run with a specific Spring profile (required — no default profile works standalone)
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=springboot
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=frameworks
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=webserver
```

**Note:** Running without an active Spring profile will fail at startup because `LoadTestRunnerProperties.appContainers` has `@NotNull`/`@NotEmpty` validation with no default value. Always pass a profile.

---

## Available Spring Profiles

Defined in `src/main/resources/application.yaml`:

| Profile | Description |
|---|---|
| `springboot` | Benchmarks Spring Boot variants (MVC, WebFlux, etc.) |
| `frameworks` | Benchmarks Spring Boot vs Quarkus vs Micronaut |
| `webserver` | Benchmarks webserver configurations |

---

## Project Structure

```
src/main/java/com/ivanfranchin/apiohabenchmarker/
├── ApiOhaBenchmarkerApplication.java   # Entry point (@SpringBootApplication, @ConfigurationPropertiesScan)
├── browser/
│   └── BrowserOpener.java              # Opens cAdvisor UI in the system browser
├── container/
│   ├── AppContainer.java               # GenericContainer wrapper for benchmarked apps
│   └── CadvisorContainer.java          # GenericContainer wrapper for cAdvisor
├── processor/
│   ├── DockerStatsProcessor.java       # Background thread streaming `docker stats`
│   └── OhaProcessor.java              # Runs `oha` CLI, parses plain-text output line-by-line
├── properties/
│   ├── AppContainerConfig.java         # Record: per-container YAML config (dockerImageName, environment, exposedPort, appType, network)
│   ├── AppType.java                    # Enum: SPRING_BOOT, QUARKUS, MICRONAUT
│   ├── CadvisorProperties.java         # Record: @ConfigurationProperties("cadvisor")
│   ├── LoadTestRunnerProperties.java   # Class: @ConfigurationProperties("load-test-runner")
│   └── OhaParameter.java              # Record: numRequests, concurrency, endpoint
├── result/
│   ├── AppResult.java                  # Record: startUpTime, cpu, mem, oha results
│   └── LoadTestResult.java            # Record: per-test oha metrics
├── runner/
│   └── LoadTestRunner.java            # CommandLineRunner: orchestrates the full benchmark
└── writer/
    └── ResultFileWriter.java          # @Component: writes results to .txt file
```

---

## Code Style Guidelines

### Formatting

- **2-space indentation** (no tabs).
- Opening braces on the **same line** (K&R style).
- Single blank line between methods.
- No trailing whitespace.
- Formatting is enforced automatically by the Spotless Maven plugin (google-java-format, GOOGLE style):
  - Check: `./mvnw spotless:check`
  - Fix:   `./mvnw spotless:apply`
- The `spotless:check` goal is bound to the `verify` phase and runs automatically with `./mvnw verify`.

### Imports

- No wildcard imports — all imports must be **explicit**.
- No static imports.
- Organize imports in this order:
  1. `java.*`
  2. `jakarta.*`
  3. `org.springframework.*`
  4. `org.testcontainers.*`
  5. `lombok.*`
  6. `com.github.dockerjava.*`
  7. `com.ivanfranchin.*` (internal)

### Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Classes, Records, Enums | `UpperCamelCase` | `LoadTestRunner`, `AppResult` |
| Enum values | `UPPER_SNAKE_CASE` | `SPRING_BOOT`, `QUARKUS` |
| Methods | `lowerCamelCase` | `run()`, `getStartUpTime()` |
| Fields and local variables | `lowerCamelCase` | `maxCpuUsage`, `dockerStatsProcessor` |
| Constants (`static final`) | `UPPER_SNAKE_CASE` | `OHA_COMMAND`, `DEFAULT_PORT` |
| Packages | `lowercase.dotted` | `com.ivanfranchin.apiohabenchmarker.processor` |
| YAML properties | `kebab-case` | `load-test-runner`, `docker-image-name` |
| Output files | `snake_case` with timestamp | `load_test_results_<timestamp>.txt` |

### Types and Records

- Prefer **Java records** for immutable value/data objects (`AppResult`, `LoadTestResult`, `OhaParameter`, `AppContainerConfig`, `CadvisorProperties`).
- Prefer `LinkedHashMap` when insertion-order matters for output.
- Use typed `@ConfigurationProperties` records/classes — never inject raw `@Value` strings for grouped config.
- Compile regex patterns once as `static final Pattern` constants, never inside loops or per-call.

### Lombok

- Use `@Slf4j` for logging (provides `log` field).
- Use `@RequiredArgsConstructor` for constructor injection of `final` fields.
- Use `@Getter`/`@Setter` only where needed; prefer records for read-only data.
- Do **not** use `@Data` on classes that are not pure DTOs.

### Configuration

- All configuration lives in `src/main/resources/application.yaml`.
- Use `---` YAML document separators with `spring.config.activate.on-profile` for profile-specific config.
- Bind configuration via `@ConfigurationProperties` classes/records, registered via `@ConfigurationPropertiesScan` on the main application class.
- Annotate `@ConfigurationProperties` beans with Jakarta Validation annotations (`@NotNull`, `@NotEmpty`, `@Valid`, `@Min`) where constraints are required; use Spring's `@Validated` on the class to activate them.

---

## Error Handling Patterns

Follow the patterns already established in the codebase:

```java
// IOException → wrap and rethrow
} catch (IOException e) {
    throw new RuntimeException(e);
}

// InterruptedException → restore interrupt flag, then rethrow
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new RuntimeException(e);
}

// Background thread errors → log and swallow (do not crash the thread)
} catch (Exception e) {
    log.error("Unable to run docker stats", e);
}

// Cleanup in finally
} finally {
    if (process != null) {
        process.destroyForcibly();
    }
}
```

- **Do not define custom exception classes** unless there is a clear domain reason. Use `RuntimeException` wrapping for infrastructure failures.
- Use sentinel return values (e.g., `-1.0`) combined with a `log.error(...)` when a parsing failure is non-fatal.
- Never silently swallow exceptions in non-background-thread contexts.

---

## Testing

- Test framework: **JUnit 5 (Jupiter)** + **Spring Boot Test**.
- Test sources are in `src/test/java/com/ivanfranchin/apiohabenchmarker/`.
- `TestcontainersConfiguration` is a `@TestConfiguration` scaffold — add container beans there for integration tests.
- `TestApiOhaBenchmarkerApplication` provides a dev-mode runner via `SpringApplication.from(...).with(TestcontainersConfiguration.class)`.
- When writing new tests that load the Spring context, ensure a valid Spring profile is active (or provide a test-specific `application-test.yaml`) so that `@NotNull` properties are satisfied.
- Run a single test method: `./mvnw test -Dtest=ClassName#methodName`

---

## Architecture Notes

- **No web layer:** There is no `spring-boot-starter-web` or `spring-boot-starter-webflux`. Do not add one unless the project scope changes.
- **Testcontainers in production:** `AppContainer` and `CadvisorContainer` use Testcontainers as a programmatic Docker API at runtime, not just in tests.
- **External process integration:** `OhaProcessor` and `DockerStatsProcessor` use `ProcessBuilder` to shell out to `oha` and `docker`. Keep external tool calls isolated in the `processor/` package.
- **Thread safety:** `DockerStatsProcessor` runs in a background `Thread`. Use a `volatile boolean` stop flag for cross-thread signaling. Always call `Thread.join()` before reading results.
- **Ordered output:** Use `LinkedHashMap` wherever declaration order must match output order.
