# api-oha-benchmarker

`api-oha-benchmarker` is a tool to easily benchmark APIs. It uses [`Testcontainers`](https://testcontainers.com/) to manage Docker containers. The load testing is done with [`OHA`](https://github.com/hatoo/oha). To collect information like CPU and memory usage, it uses the [`docker stats`](https://docs.docker.com/reference/cli/docker/container/stats/) command. It also uses [`cAdvisor`](https://github.com/google/cadvisor) to visually monitor CPU and memory usage.

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in by using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

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
  ./mvnw clean spring-boot:run
  ```
