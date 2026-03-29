package com.ivanfranchin.apiohabenchmarker.browser;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ivanfranchin.apiohabenchmarker.properties.CadvisorProperties;

@Slf4j
@RequiredArgsConstructor
@Component
public class BrowserOpener {

  private final CadvisorProperties cadvisorProperties;

  public void open(String containerId, Integer containerMappedPort) {
    try {
      String browserCommand =
          cadvisorProperties.browserCommand().formatted(containerId, containerMappedPort);
      ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", browserCommand);
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
      log.debug("Exited with  code: {}", exitCode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
