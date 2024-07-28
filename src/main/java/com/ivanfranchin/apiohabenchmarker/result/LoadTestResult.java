package com.ivanfranchin.apiohabenchmarker.result;

public record LoadTestResult(int numRequests, int concurrency, double[] ohaMetrics) {
}
