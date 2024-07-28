package com.ivanfranchin.apiohabenchmarker.writer;

import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ResultFileWriter {

    private static final Path FILE_PATH = Paths.get("load_test_results_%s.txt".formatted(System.currentTimeMillis()));
    private static final String SEPARATOR = "----------------------------------------";

    public static void write(Map<String, AppResult> appResultMap) {
        writeValuedToFile(SEPARATOR, StandardOpenOption.CREATE_NEW);
        writeAllOhaMetrics(appResultMap);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeStartUpTimeAndMaxCpuAndMaxMemUsage(appResultMap);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeStatUpTime(appResultMap);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeMaxCpuUsage(appResultMap);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeMaxMemUsage(appResultMap);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeSpecificOhaMetric(appResultMap, "Total(secs)", 1);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeSpecificOhaMetric(appResultMap, "Average(secs)", 4);
        writeValuedToFile(SEPARATOR, StandardOpenOption.APPEND);
        writeSpecificOhaMetric(appResultMap, "Requests/sec", 5);
    }

    private static void writeStartUpTimeAndMaxCpuAndMaxMemUsage(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tStatUpTime(sec)\tMax CPU(%)\tMax Memory(MB)", StandardOpenOption.APPEND);
        for (String appName : appResultMap.keySet()) {
            AppResult appResult = appResultMap.get(appName);
            String line = String.format(Locale.US, "%s\t%.4f\t%.2f\t%.2f",
                    appName, appResult.startUpTime(), appResult.maxCpuUsage(), appResult.maxMemUsage());
            writeValuedToFile(line, StandardOpenOption.APPEND);
        }
    }

    private static void writeStatUpTime(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tStatUpTime(sec)", StandardOpenOption.APPEND);
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.4f", appName, appResultMap.get(appName).startUpTime());
            writeValuedToFile(line, StandardOpenOption.APPEND);
        }
    }

    private static void writeMaxCpuUsage(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tMax CPU(%)", StandardOpenOption.APPEND);
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", appName, appResultMap.get(appName).maxCpuUsage());
            writeValuedToFile(line, StandardOpenOption.APPEND);
        }
    }

    private static void writeMaxMemUsage(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tMax Memory(MB)", StandardOpenOption.APPEND);
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", appName, appResultMap.get(appName).maxMemUsage());
            writeValuedToFile(line, StandardOpenOption.APPEND);
        }
    }

    private static void writeSpecificOhaMetric(Map<String, AppResult> appResultMap, String metric, int col) {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        Set<String> numRequestAndConcurrencySet = new LinkedHashSet<>();
        for (String appName : appResultMap.keySet()) {
            for (LoadTestResult result : appResultMap.get(appName).loadTestResults()) {
                map.computeIfAbsent(appName, k -> new LinkedList<>()).add(result.ohaMetrics()[col]);
                numRequestAndConcurrencySet.add(result.numRequests() == result.concurrency() ?
                        String.valueOf(result.numRequests()) : "%s/%s".formatted(result.numRequests(), result.concurrency()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(metric);
        for (String numRequestAndConcurrency : numRequestAndConcurrencySet) {
            sb.append(String.format("\t%s", numRequestAndConcurrency));
        }
        writeValuedToFile(sb.toString(), StandardOpenOption.APPEND);

        for (String appName : map.keySet()) {
            sb = new StringBuilder();
            sb.append(appName);
            for (Double val : map.get(appName)) {
                sb.append(String.format(Locale.US, "\t%.4f", val));
            }
            writeValuedToFile(sb.toString(), StandardOpenOption.APPEND);
        }
    }

    private static void writeAllOhaMetrics(Map<String, AppResult> appResultMap) {
        String header = "Application\tnumRequests\tConcurrency\tSuccess rate(%)\tTotal(secs)\tSlowest(secs)\tFastest(secs)\tAverage(secs)\tRequests/sec";
        writeValuedToFile(header, StandardOpenOption.APPEND);

        for (String appName : appResultMap.keySet()) {
            for (LoadTestResult result : appResultMap.get(appName).loadTestResults()) {
                String line = String.format(
                        Locale.US,
                        "%s\t%s\t%s\t%.2f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f",
                        appName,
                        result.numRequests(),
                        result.concurrency(),
                        result.ohaMetrics()[0],
                        result.ohaMetrics()[1],
                        result.ohaMetrics()[2],
                        result.ohaMetrics()[3],
                        result.ohaMetrics()[4],
                        result.ohaMetrics()[5]
                );
                writeValuedToFile(line, StandardOpenOption.APPEND);
            }
        }
    }

    private static void writeValuedToFile(String line, OpenOption... openOption) {
        try {
            Files.writeString(FILE_PATH, line + System.lineSeparator(), openOption);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
