package com.ivanfranchin.apiohabenchmarker.result;

public record LoadTestResult(int numRequests, int concurrency, String endpoint, double[] ohaMetrics) {
}
