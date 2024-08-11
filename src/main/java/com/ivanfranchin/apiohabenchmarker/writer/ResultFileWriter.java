package com.ivanfranchin.apiohabenchmarker.writer;

import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;

import java.io.IOException;
import java.nio.file.Files;
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

    public static void write(Map<String, AppResult> appResultMap) {
        createNewFile();
        writeAllOhaMetrics(appResultMap);
        newLine();
        writeStartUpTimeAndMaxCpuAndMaxMemUsage(appResultMap);
        newLine();
        writeStartUpTime(appResultMap);
        newLine();
        writeMaxCpuUsage(appResultMap);
        newLine();
        writeMaxMemUsage(appResultMap);
        newLine();
        writeSpecificOhaMetric(appResultMap, "Total(secs)", 1);
        newLine();
        writeSpecificOhaMetric(appResultMap, "Average(secs)", 4);
        newLine();
        writeSpecificOhaMetric(appResultMap, "Requests/sec", 5);
    }

    private static void writeStartUpTimeAndMaxCpuAndMaxMemUsage(Map<String, AppResult> appResultMap) {
        String fmtHeader = "%25s | %16s | %11s | %14s |";
        String fmtDivisor = "%25s + %16s + %11s + %14s |";
        String fmtMetric = "%25s | %16.4f | %11.2f | %14.2f |";

        String header = fmtHeader.formatted("Application", "StartUpTime(sec)", "Max CPU(%)", "Max Memory(MB)");
        writeValuedToFile(header);

        String divisor = fmtDivisor.formatted(hdChars(25), hdChars(16), hdChars(11), hdChars(14));
        writeValuedToFile(divisor);

        for (String appName : appResultMap.keySet()) {
            AppResult appResult = appResultMap.get(appName);
            String line = String.format(Locale.US, fmtMetric,
                    appName, appResult.startUpTime(), appResult.maxCpuUsage(), appResult.maxMemUsage());
            writeValuedToFile(line);
        }
    }

    private static void writeStartUpTime(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tStartUpTime(sec)");
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.4f", appName, appResultMap.get(appName).startUpTime());
            writeValuedToFile(line);
        }
    }

    private static void writeMaxCpuUsage(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tMax CPU(%)");
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", appName, appResultMap.get(appName).maxCpuUsage());
            writeValuedToFile(line);
        }
    }

    private static void writeMaxMemUsage(Map<String, AppResult> appResultMap) {
        writeValuedToFile("Application\tMax Memory(MB)");
        for (String appName : appResultMap.keySet()) {
            String line = String.format(Locale.US, "%s\t%.2f", appName, appResultMap.get(appName).maxMemUsage());
            writeValuedToFile(line);
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
        writeValuedToFile(sb.toString());

        for (String appName : map.keySet()) {
            sb = new StringBuilder();
            sb.append(appName);
            for (Double val : map.get(appName)) {
                sb.append(String.format(Locale.US, "\t%.4f", val));
            }
            writeValuedToFile(sb.toString());
        }
    }

    private static void writeAllOhaMetrics(Map<String, AppResult> appResultMap) {
        String fmtHeader = "%25s | %11s | %11s | %15s | %11s | %13s | %13s | %13s | %12s |";
        String fmtDivisor = "%25s + %11s + %11s + %15s + %11s + %13s + %13s + %13s + %12s |";
        String fmtMetric = "%25s | %11d | %11d | %15.2f | %11.4f | %13.4f | %13.4f | %13.4f | %12.4f |";

        String header = fmtHeader.formatted("Application", "numRequests", "Concurrency", "Success rate(%)", "Total(secs)", "Slowest(secs)", "Fastest(secs)", "Average(secs)", "Requests/sec");
        writeValuedToFile(header);

        String divisor = fmtDivisor.formatted(hdChars(25), hdChars(11), hdChars(11), hdChars(15), hdChars(11), hdChars(13), hdChars(13), hdChars(13), hdChars(12));
        writeValuedToFile(divisor);

        int count = 0;
        for (String appName : appResultMap.keySet()) {
            for (LoadTestResult result : appResultMap.get(appName).loadTestResults()) {
                String line = String.format(
                        Locale.US,
                        fmtMetric,
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
                writeValuedToFile(line);
            }
            count++;
            if (count < appResultMap.size()) {
                divisor = fmtDivisor.formatted(mdChars(25), mdChars(11), mdChars(11), mdChars(15), mdChars(11), mdChars(13), mdChars(13), mdChars(13), mdChars(12));
                writeValuedToFile(divisor);
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

    private static void createNewFile() {
        try {
            Files.createFile(FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void newLine() {
        writeValuedToFile("");
    }

    private static void writeValuedToFile(String line) {
        try {
            Files.writeString(FILE_PATH, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
