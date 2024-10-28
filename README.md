# api-oha-benchmarker

`api-oha-benchmarker` is a tool to easily benchmark APIs. It uses [`Testcontainers`](https://testcontainers.com/) to manage Docker containers. The load testing is done with [`OHA`](https://github.com/hatoo/oha). To collect information like CPU and memory usage, it uses the [`docker stats`](https://docs.docker.com/reference/cli/docker/container/stats/) command. It also uses [`cAdvisor`](https://github.com/google/cadvisor) to visually monitor CPU and memory usage.

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in by using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

## Additional Readings

### Spring Boot Performance Benchmark

- \[**Medium**\] [**Spring Boot Performance Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-performance-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-29295c8099b0)
- \[**Medium**\] [**Spring Boot 3.3.2 Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-3-3-2-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-42d3b704e88e)
- \[**Medium**\] [**Spring Boot 3.3.4 Benchmark: Web, Reactive, CDS, AOT, Virtual Threads, JVM, and Native**](https://medium.com/@ivangfr/spring-boot-3-3-4-benchmark-web-reactive-cds-aot-virtual-threads-jvm-and-native-5a3ab117054c)

### Java Frameworks Performance Benchmark

- \[**Medium**\] [**Java Frameworks Performance Benchmark: Spring Boot vs. Quarkus vs. Micronaut**](https://medium.com/@ivangfr/java-frameworks-performance-benchmark-spring-boot-vs-quarkus-vs-micronaut-028b6dbfef2e)
- \[**Medium**\] [**Performance Benchmark: Spring Boot 3.3.2 vs. Quarkus 3.13.2 vs. Micronaut 4.5.1**](https://medium.com/@ivangfr/performance-benchmark-spring-boot-3-3-2-vs-quarkus-3-13-2-vs-micronaut-4-5-1-515bae82d04f)
- \[**Medium**\] [**Performance Benchmark: Spring Boot 3.3.4 vs. Quarkus 3.15.1 vs. Micronaut 4.6.3**](https://medium.com/@ivangfr/performance-benchmark-spring-boot-3-3-4-vs-quarkus-3-15-1-vs-micronaut-4-6-3-9691c4cfcb2a)

## Prerequisites

- [`Java 21+`](https://www.oracle.com/java/technologies/downloads/#java21)
- [`Docker`](https://www.docker.com/)

## Configuration

All the configuration is done in the `application.yaml` where you can set:

- Should `cAdvisor` open the webpage with container statistics?
  - Property: `cadvisor.browser-opener.enabled`
  - Default: `false`
- Number of requests and concurrency:
  - Property: `load-test-runner.num-requests-and-concurrency`
  - Default: list containing `100:100`, `300:300`, `900:900`, and `2700:2700`
- Pause between request submissions:
  - Property: `load-test-runner.pause-millis`
  - Default: `3000`
- Applications Docker containers to run:
  - Property: `load-test-runner.app-containers`
  - Configuration map:
    ```
    <docker-container-name>:
      docker-image-name: <name of the Docker image to use>
      endpoint: <endpoint to call>
      app-type: <application type: spring-boot, quarkus, or micronaut>
      environment: <list of environment variables for the container>
    ```

## How to run

- In a terminal, make sure you are inside the `api-oha-benchmarker` root folder;
- Run the following command:
  ```
  ./mvnw clean spring-boot:run -Dspring-boot.run.profiles=springboot
  ```
