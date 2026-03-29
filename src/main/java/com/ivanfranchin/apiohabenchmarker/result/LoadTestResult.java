package com.ivanfranchin.apiohabenchmarker.result;

import java.util.List;

public record LoadTestResult(int numRequests, int concurrency, String endpoint, List<Double> ohaMetrics) {
}
