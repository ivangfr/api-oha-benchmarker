package com.ivanfranchin.apiohabenchmarker.browser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BrowserOpener {

    private static final String BROWSER_COMMAND = "open -a \"Brave Browser\" -n --args --incognito http://localhost:%s/docker/%s";

    @Value("${cadvisor.browser-opener.enabled}")
    private boolean isEnabled;

    public void open(String containerId, Integer containerMappedPort) {
        if (isEnabled) {
            try {
                String browserCommand = BROWSER_COMMAND.formatted(containerMappedPort, containerId);
                ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", browserCommand);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                log.debug("Exited with code: {}", exitCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
