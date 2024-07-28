package com.ivanfranchin.apiohabenchmarker.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AppContainerConfig(

        @NotBlank
        String dockerImageName,

        List<String> environment,
        Integer exposedPort,

        @NotBlank
        String endpoint,

        @NotNull
        AppType appType
) {
}
