package com.ivanfranchin.apiohabenchmarker.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "load-test-runner")
public class LoadTestRunnerProperties {

    @Min(1000)
    private Integer pauseMillis;

    @NotNull
    private List<OhaParameter> ohaParameters;

    @NotNull
    private Map<String, AppContainerConfig> appContainers;
}
