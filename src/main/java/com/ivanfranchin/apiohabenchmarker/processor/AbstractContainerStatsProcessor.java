package com.ivanfranchin.apiohabenchmarker.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractContainerStatsProcessor implements ContainerStatsProcessor {

  @Getter private double maxCpuUsage = -1.0;
  @Getter private double maxMemUsage = -1.0;

  private volatile boolean stop;

  protected final String containerName;

  protected AbstractContainerStatsProcessor(String containerName) {
    this.containerName = containerName;
  }

  @Override
  public void stop() {
    this.stop = true;
  }

  protected abstract String[] buildCommand(String containerName);

  protected abstract Pattern getPattern();

  protected abstract double toMiB(double rawMem, String unit);

  protected abstract String toolName();

  @Override
  public void run() {
    ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(containerName));
    Process process = null;
    try {
      process = processBuilder.start();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          Matcher matcher = getPattern().matcher(line);
          if (matcher.find()) {
            double cpuUsage = Double.parseDouble(matcher.group(1));
            double rawMem = Double.parseDouble(matcher.group(2));
            double memUsage = toMiB(rawMem, matcher.group(3));
            maxCpuUsage = Math.max(maxCpuUsage, cpuUsage);
            maxMemUsage = Math.max(maxMemUsage, memUsage);
            log.debug(
                "CPU Usage: {}, Memory Usage: {}, Max CPU Usage: {}, Max Memory Usage: {}",
                cpuUsage,
                memUsage,
                maxCpuUsage,
                maxMemUsage);
          }
          if (stop) {
            break;
          }
        }
      }
    } catch (Exception e) {
      log.error("Unable to run {} stats", toolName(), e);
    } finally {
      if (process != null) {
        process.destroyForcibly();
      }
    }
  }
}
