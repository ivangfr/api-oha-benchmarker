package com.ivanfranchin.apiohabenchmarker.processor;

import java.util.regex.Pattern;

public class DockerStatsProcessor extends AbstractContainerStatsProcessor {

  public DockerStatsProcessor(String containerName) {
    super(containerName);
  }

  @Override
  protected String[] buildCommand(String containerName) {
    return new String[] {
      "docker", "container", "stats", containerName, "--format", "{{.CPUPerc}} {{.MemUsage}}"
    };
  }

  @Override
  protected Pattern getPattern() {
    return PATTERN;
  }

  @Override
  protected double toMiB(double rawMem, String unit) {
    return "GiB".equals(unit) ? rawMem * 1024 : rawMem;
  }

  @Override
  protected String toolName() {
    return "docker";
  }

  private static final Pattern PATTERN =
      Pattern.compile("(\\d+\\.\\d+)%\\s+(\\d+\\.\\d+)(MiB|GiB)");
}
