package com.ivanfranchin.apiohabenchmarker.result;

import java.util.List;

public record AppResult(
        double startUpTime,
        double maxCpuUsage,
        double maxMemUsage,
        List<LoadTestResult> loadTestResults
) {
}
