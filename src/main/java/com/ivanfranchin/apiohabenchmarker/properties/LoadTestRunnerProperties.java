package com.ivanfranchin.apiohabenchmarker.properties;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "load-test-runner")
public class LoadTestRunnerProperties {

  @Min(3000)
  private Integer waitForContainerToStartMillis;

  @Min(1000)
  private Integer pauseBetweenTestsMillis;

  @NotNull private ContainerRuntime containerRuntime;

  @NotNull private List<OhaParameter> ohaParameters;

  @NotNull @NotEmpty private Map<String, AppContainerConfig> appContainers;
}
