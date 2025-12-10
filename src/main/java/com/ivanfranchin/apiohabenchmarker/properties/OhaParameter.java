package com.ivanfranchin.apiohabenchmarker.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OhaParameter(
        @Positive Integer numRequests,
        @Positive Integer concurrency,
        @NotNull String endpoint) {
}
