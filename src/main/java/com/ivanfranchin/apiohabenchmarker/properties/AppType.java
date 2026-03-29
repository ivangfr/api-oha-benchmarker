package com.ivanfranchin.apiohabenchmarker.properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AppType {
  SPRING_BOOT(Pattern.compile("in ([\\d.]+) seconds"), 1.0),
  QUARKUS(Pattern.compile("started in ([\\d.]+)s"), 1.0),
  MICRONAUT(Pattern.compile("Startup completed in ([\\d.]+)ms"), 1000.0);

  private final Pattern pattern;
  private final double divisor;

  AppType(Pattern pattern, double divisor) {
    this.pattern = pattern;
    this.divisor = divisor;
  }

  public double parseStartUpTime(String logs) {
    Matcher matcher = pattern.matcher(logs);
    if (matcher.find()) {
      return Double.parseDouble(matcher.group(1)) / divisor;
    }
    return -1.0;
  }
}
