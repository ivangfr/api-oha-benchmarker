package com.ivanfranchin.apiohabenchmarker.processor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DockerStatsProcessor implements Runnable {

    @Getter
    private double maxCpuUsage = -1.0;
    @Getter
    private double maxMemUsage = -1.0;

    private boolean stop;

    public void stop() {
        this.stop = true;
    }

    private final String containerName;

    public DockerStatsProcessor(String containerName) {
        this.containerName = containerName;
    }

    @Override
    public void run() {
        try {
            String[] command = {"docker", "container", "stats", containerName, "--format", "{{.CPUPerc}} {{.MemUsage}}"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PATTERN.matcher(line);
                    if (matcher.find()) {
                        double cpuUsage = Double.parseDouble(matcher.group(1));
                        double memUsage = Double.parseDouble(matcher.group(2));
                        maxCpuUsage = Math.max(maxCpuUsage, cpuUsage);
                        maxMemUsage = Math.max(maxMemUsage, memUsage);
                        log.debug("CPU Usage: {}, Memory Usage: {}, Max CPU Usage: {}, Max Memory Usage: {}",
                                cpuUsage, memUsage, maxCpuUsage, maxMemUsage);
                    }
                    if (stop) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to run docker stats", e);
        }
    }

    private static final String REGEX = "(\\d+\\.\\d+)%\\s+(\\d+\\.\\d+)MiB";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

}
