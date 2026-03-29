package com.ivanfranchin.apiohabenchmarker.processor;

import java.util.regex.Pattern;

public class PodmanStatsProcessor extends AbstractContainerStatsProcessor {

  public PodmanStatsProcessor(String containerName) {
    super(containerName);
  }

  @Override
  protected String[] buildCommand(String containerName) {
    return new String[] {
      "podman", "container", "stats", containerName, "--format", "{{.CPUPerc}} {{.MemUsage}}"
    };
  }

  @Override
  protected Pattern getPattern() {
    return PATTERN;
  }

  @Override
  protected double toMiB(double rawMem, String unit) {
    // Podman reports in decimal units; convert to MiB
    return switch (unit) {
      case "GB" -> rawMem * 1000 / 1.048576;
      case "kB" -> rawMem / 1024;
      default -> rawMem / 1.048576; // MB
    };
  }

  @Override
  protected String toolName() {
    return "podman";
  }

  private static final Pattern PATTERN =
      Pattern.compile("(\\d+\\.\\d+)%\\s+(\\d+\\.\\d+)(MB|GB|kB)");
}
