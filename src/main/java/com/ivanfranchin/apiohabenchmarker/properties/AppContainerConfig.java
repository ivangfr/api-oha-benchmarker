package com.ivanfranchin.apiohabenchmarker.properties;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppContainerConfig(
    @NotBlank String dockerImageName,
    List<String> environment,
    Integer exposedPort,
    @NotNull AppType appType,
    String network) {}
