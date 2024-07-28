package com.ivanfranchin.apiohabenchmarker;

import org.springframework.boot.SpringApplication;

public class TestApiOhaBenchmarkerApplication {

    public static void main(String[] args) {
        SpringApplication.from(ApiOhaBenchmarkerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
