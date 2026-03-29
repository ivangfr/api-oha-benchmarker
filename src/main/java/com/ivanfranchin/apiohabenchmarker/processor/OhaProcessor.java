package com.ivanfranchin.apiohabenchmarker.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OhaProcessor {

    private static final String OHA_COMMAND = "oha -n %s -c %s -u s --latency-correction --disable-keepalive --disable-color --no-tui http://localhost:%s/%s";

    public List<Double> run(int numRequests, int concurrency, int containerMappedPort, String endpoint) {
        List<Double> ohaMetrics;
        try {
            String ohaCommand = OHA_COMMAND.formatted(numRequests, concurrency, containerMappedPort, endpoint);
            log.info("oha command: {}", ohaCommand);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", ohaCommand);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                ohaMetrics = processLines(reader);
            }
            int exitCode = process.waitFor();
            log.debug("Exited with code: {}", exitCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return ohaMetrics;
    }

    private List<Double> processLines(BufferedReader reader) throws IOException {
        List<Double> metrics = new ArrayList<>(Collections.nCopies(LINES_TO_PARSE.length, 0.0));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("\\x1B\\[[;\\d]*m", "").trim();
            int idx = findColumn(line);
            if (idx >= 0) {
                log.info(line);
                metrics.set(idx, parseValue(line));
            }
        }
        return Collections.unmodifiableList(metrics);
    }

    private double parseValue(String line) {
        int colonIdx = line.indexOf(':');
        int percentIdx = line.indexOf('%');
        int secIdx = line.indexOf(" sec");
        String val;
        if (percentIdx >= 0) {
            val = line.substring(colonIdx + 1, percentIdx);
        } else if (secIdx >= 0) {
            val = line.substring(colonIdx + 1, secIdx);
        } else {
            val = line.substring(colonIdx + 1);
        }
        return Double.parseDouble(val.trim());
    }

    private int findColumn(String line) {
        for (int i = 0; i < LINES_TO_PARSE.length; i++) {
            if (line.startsWith(LINES_TO_PARSE[i])) {
                return i;
            }
        }
        return -1;
    }

    private static final String[] LINES_TO_PARSE = new String[]{
            "Success rate:",
            "Total:",
            "Slowest:",
            "Fastest:",
            "Average:",
            "Requests/sec:"
    };
}
