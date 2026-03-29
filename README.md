# api-oha-benchmarker

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-ivan.franchin-FFDD00?logo=buymeacoffee&logoColor=black)](https://buymeacoffee.com/ivan.franchin)

`api-oha-benchmarker` is a CLI-only Spring Boot app that automates HTTP API benchmarking. It uses [`Testcontainers`](https://testcontainers.com/) to manage Docker containers for apps under test, runs load tests with [`OHA`](https://github.com/hatoo/oha), collects CPU/memory metrics via [`docker stats`](https://docs.docker.com/reference/cli/docker/container/stats/), and optionally launches [`cAdvisor`](https://github.com/google/cadvisor) for visual monitoring. Results are written to a `.txt` file.

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

## Additional Readings

### Spring Boot Performance Benchmark

- \[**Medium**\] [**Spring Boot Performance Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-performance-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-29295c8099b0)
- \[**Medium**\] [**Spring Boot 3.3.2 Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-3-3-2-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-42d3b704e88e)
- \[**Medium**\] [**Spring Boot 3.3.4 Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-3-3-4-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-5a3ab117054c)
- \[**Medium**\] [**Spring Boot 3.4.3 Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-3-4-3-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-47bff836992e)

### Java Frameworks Performance Benchmark

- \[**Medium**\] [**Java Frameworks Performance Benchmark: Spring Boot vs. Quarkus vs. Micronaut**](https://medium.com/@ivangfr/java-frameworks-performance-benchmark-spring-boot-vs-quarkus-vs-micronaut-028b6dbfef2e)
- \[**Medium**\] [**Performance Benchmark: Spring Boot 3.3.2 vs. Quarkus 3.13.2 vs. Micronaut 4.5.1**](https://medium.com/@ivangfr/performance-benchmark-spring-boot-3-3-2-vs-quarkus-3-13-2-vs-micronaut-4-5-1-515bae82d04f)
- \[**Medium**\] [**Performance Benchmark: Spring Boot 3.3.4 vs. Quarkus 3.15.1 vs. Micronaut 4.6.3**](https://medium.com/@ivangfr/performance-benchmark-spring-boot-3-3-4-vs-quarkus-3-15-1-vs-micronaut-4-6-3-9691c4cfcb2a)
- \[**Medium**\] [**Performance Benchmark: Spring Boot 3.4.3 vs. Quarkus 3.19.3 vs. Micronaut 4.7.6**](https://medium.com/@ivangfr/performance-benchmark-spring-boot-3-4-3-vs-quarkus-3-19-3-vs-micronaut-4-7-6-aaadfb0382b4)

### Embedded Web Server Benchmark

- \[**Medium**\] [**What is the Best Embedded Web Server for Spring Boot version 3.3.2: Tomcat vs. Jetty vs. Undertow**](https://medium.com/@ivangfr/choosing-the-best-embedded-web-server-for-your-spring-boot-app-tomcat-vs-jetty-vs-undertow-0086427d124e)
- \[**Medium**\] [**What is the Best Embedded Web Server for Spring Boot version 3.4.4: Tomcat vs. Jetty vs. Undertow**](https://medium.com/@ivangfr/what-is-the-best-embedded-web-server-for-spring-boot-version-3-4-4-tomcat-vs-jetty-vs-undertow-c9186a510301)

## Prerequisites

- [`Java 25`](https://www.oracle.com/java/technologies/downloads/#java25) or higher.
- A containerization tool (e.g., [`Docker`](https://www.docker.com), [`Podman`](https://podman.io), etc.)
- [`oha`](https://github.com/hatoo/oha)

## Configuration

All configuration lives in `src/main/resources/application.yaml`. The application **requires** an active Spring profile to start — running without one will fail at startup because the `load-test-runner.app-containers` property has no default value.

### Available profiles

| Profile | Description |
|---|---|
| `springboot` | Benchmarks Spring Boot variants (MVC, WebFlux, CDS, AOT, virtual threads, JVM, native) |
| `frameworks` | Benchmarks Spring Boot vs. Quarkus vs. Micronaut |
| `webserver` | Benchmarks embedded web server configurations (Tomcat vs. Jetty vs. Undertow) |

### Key configuration properties

- **`cadvisor.enabled`** — Start a cAdvisor container for visual monitoring (default: `false`)
- **`cadvisor.open-browser`** — Automatically open the cAdvisor UI in the browser (default: `false`)
- **`load-test-runner.num-requests-and-concurrency`** — List of `numRequests:concurrency` pairs (default: `100:100`, `300:300`, `900:900`, `2700:2700`)
- **`load-test-runner.pause-millis`** — Pause in milliseconds between load-test submissions (default: `3000`)
- **`load-test-runner.container-memory`** — Memory limit applied to each benchmarked container (default: `1GB`). Accepts standard suffixes: `MB`, `GB`.
- **`load-test-runner.app-containers`** — Map of Docker containers to benchmark. Each entry supports:
  ```yaml
  <docker-container-name>:
    docker-image-name: <Docker image>
    endpoint: <path to call during load test>
    app-type: <spring-boot | quarkus | micronaut>
    environment: <list of environment variables for the container>
  ```

## How to run

Open a terminal, make sure you are inside the `api-oha-benchmarker` root folder, then run with the desired profile:

```bash
# Benchmark Spring Boot variants
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=springboot

# Benchmark Spring Boot vs. Quarkus vs. Micronaut
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=frameworks

# Benchmark embedded web servers
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=webserver
```

Results are written to a `load_test_results_<timestamp>.txt` file in the project root.

## Code Formatting

This project enforces consistent Java formatting using the [Spotless](https://github.com/diffplug/spotless) Maven plugin with [google-java-format](https://github.com/google/google-java-format) (GOOGLE style).

**Check formatting:**
```bash
./mvnw spotless:check
```

**Auto-fix formatting:**
```bash
./mvnw spotless:apply
```

Formatting is also verified automatically as part of `./mvnw verify` (bound to the `verify` phase).

## Support

If you find this useful, consider buying me a coffee:

<a href="https://buymeacoffee.com/ivan.franchin"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="50"></a>

## License

This project is licensed under the [MIT License](./LICENSE).
