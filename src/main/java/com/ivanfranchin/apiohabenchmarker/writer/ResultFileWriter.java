package com.ivanfranchin.apiohabenchmarker.writer;

import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ResultFileWriter {

    public void write(Map<String, AppResult> appResultMap) {
        Path filePath = Paths.get("load_test_results_%s.txt".formatted(System.currentTimeMillis()));
        createNewFile(filePath);
        writeAllOhaMetrics(filePath, appResultMap);
        newLine(filePath);
        writeStartUpTimeAndMaxCpuAndMaxMemUsage(filePath, appResultMap);
        newLine(filePath);
        writeStartUpTime(filePath, appResultMap);
        newLine(filePath);
        writeMaxCpuUsage(filePath, appResultMap);
        newLine(filePath);
        writeMaxMemUsage(filePath, appResultMap);
        newLine(filePath);
        writeSpecificOhaMetric(filePath, appResultMap, "Total(sec)", 1);
        newLine(filePath);
        writeSpecificOhaMetric(filePath, appResultMap, "Average(sec)", 4);
        newLine(filePath);
        writeSpecificOhaMetric(filePath, appResultMap, "Requests/sec", 5);
    }

    private void writeStartUpTimeAndMaxCpuAndMaxMemUsage(Path filePath, Map<String, AppResult> appResultMap) {
        String fmtHeader = "%25s | %16s | %10s | %14s |";
        String fmtDivisor = "%25s + %16s + %10s + %14s |";
        String fmtMetric = "%25s | %16.4f | %10.2f | %14.2f |";

        String header = fmtHeader.formatted("Application", "StartUpTime(sec)", "Max CPU(%)", "Max Memory(MB)");
        writeValuedToFile(filePath, header);

        String divisor = fmtDivisor.formatted(hdChars(25), hdChars(16), hdChars(10), hdChars(14));
        writeValuedToFile(filePath, divisor);

        for (Map.Entry<String, AppResult> entry : appResultMap.entrySet()) {
            String appName = entry.getKey();
            AppResult appResult = entry.getValue();
            String line = String.format(Locale.US, fmtMetric,
                    appName, appResult.startUpTime(), appResult.maxCpuUsage(), appResult.maxMemUsage());
            writeValuedToFile(filePath, line);
        }
    }

    private void writeStartUpTime(Path filePath, Map<String, AppResult> appResultMap) {
        writeValuedToFile(filePath, "Application\tStartUpTime(sec)");
        for (Map.Entry<String, AppResult> entry : appResultMap.entrySet()) {
            String line = String.format(Locale.US, "%s\t%.4f", entry.getKey(), entry.getValue().startUpTime());
            writeValuedToFile(filePath, line);
        }
    }

    private void writeMaxCpuUsage(Path filePath, Map<String, AppResult> appResultMap) {
        writeValuedToFile(filePath, "Application\tMax CPU(%)");
        for (Map.Entry<String, AppResult> entry : appResultMap.entrySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", entry.getKey(), entry.getValue().maxCpuUsage());
            writeValuedToFile(filePath, line);
        }
    }

    private void writeMaxMemUsage(Path filePath, Map<String, AppResult> appResultMap) {
        writeValuedToFile(filePath, "Application\tMax Memory(MB)");
        for (Map.Entry<String, AppResult> entry : appResultMap.entrySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", entry.getKey(), entry.getValue().maxMemUsage());
            writeValuedToFile(filePath, line);
        }
    }

    private void writeSpecificOhaMetric(Path filePath, Map<String, AppResult> appResultMap, String metric, int col) {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        Set<String> numRequestAndConcurrencySet = new LinkedHashSet<>();
        for (String appName : appResultMap.keySet()) {
            for (LoadTestResult result : appResultMap.get(appName).loadTestResults()) {
                map.computeIfAbsent(appName, k -> new ArrayList<>()).add(result.ohaMetrics().get(col));
                numRequestAndConcurrencySet.add(result.numRequests() == result.concurrency() ?
                        String.valueOf(result.numRequests()) : "%s/%s".formatted(result.numRequests(), result.concurrency()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(metric);
        for (String numRequestAndConcurrency : numRequestAndConcurrencySet) {
            sb.append(String.format("\t%s", numRequestAndConcurrency));
        }
        writeValuedToFile(filePath, sb.toString());

        for (Map.Entry<String, List<Double>> entry : map.entrySet()) {
            sb = new StringBuilder();
            sb.append(entry.getKey());
            for (Double val : entry.getValue()) {
                sb.append(String.format(Locale.US, "\t%.4f", val));
            }
            writeValuedToFile(filePath, sb.toString());
        }
    }

    private void writeAllOhaMetrics(Path filePath, Map<String, AppResult> appResultMap) {
        String fmtHeader = "%25s | %11s | %11s | %25s | %15s | %11s | %13s | %13s | %13s | %12s |";
        String fmtDivisor = "%25s + %11s + %11s + %25s + %15s + %11s + %13s + %13s + %13s + %12s |";
        String fmtMetric = "%25s | %11d | %11d | %25s | %15.2f | %11.4f | %13.4f | %13.4f | %13.4f | %12.4f |";

        String header = fmtHeader.formatted("Application", "numRequests", "Concurrency", "Endpoint", "Success rate(%)", "Total(sec)", "Slowest(sec)", "Fastest(sec)", "Average(sec)", "Requests/sec");
        writeValuedToFile(filePath, header);

        String divisor = fmtDivisor.formatted(hdChars(25), hdChars(11), hdChars(11), hdChars(25), hdChars(15), hdChars(11), hdChars(13), hdChars(13), hdChars(13), hdChars(12));
        writeValuedToFile(filePath, divisor);

        int count = 0;
        for (Map.Entry<String, AppResult> entry : appResultMap.entrySet()) {
            String appName = entry.getKey();
            for (LoadTestResult result : entry.getValue().loadTestResults()) {
                String line = String.format(
                        Locale.US,
                        fmtMetric,
                        appName,
                        result.numRequests(),
                        result.concurrency(),
                        result.endpoint(),
                         result.ohaMetrics().get(0),
                        result.ohaMetrics().get(1),
                        result.ohaMetrics().get(2),
                        result.ohaMetrics().get(3),
                        result.ohaMetrics().get(4),
                        result.ohaMetrics().get(5)
                );
                writeValuedToFile(filePath, line);
            }
            count++;
            if (count < appResultMap.size()) {
                divisor = fmtDivisor.formatted(mdChars(25), mdChars(11), mdChars(11), mdChars(25), mdChars(15), mdChars(11), mdChars(13), mdChars(13), mdChars(13), mdChars(12));
                writeValuedToFile(filePath, divisor);
            }
        }
    }

    private static String hdChars(int len) {
        return genChars(len, '-');
    }

    private static String mdChars(int len) {
        return genChars(len, '.');
    }

    private static String genChars(int len, char c) {
        return String.valueOf(c).repeat(Math.max(0, len));
    }

    private static void createNewFile(Path filePath) {
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void newLine(Path filePath) {
        writeValuedToFile(filePath, "");
    }

    private static void writeValuedToFile(Path filePath, String line) {
        try {
            Files.writeString(filePath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
