package com.ivanfranchin.apiohabenchmarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiOhaBenchmarkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiOhaBenchmarkerApplication.class, args);
  }
}
