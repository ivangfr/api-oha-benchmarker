package com.ivanfranchin.apiohabenchmarker;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Test;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApiOhaBenchmarkerApplicationTests {

  @Test
  void contextLoads() {}
}
