package com.ivanfranchin.apiohabenchmarker.runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ivanfranchin.apiohabenchmarker.browser.BrowserOpener;
import com.ivanfranchin.apiohabenchmarker.container.AppContainer;
import com.ivanfranchin.apiohabenchmarker.container.CadvisorContainer;
import com.ivanfranchin.apiohabenchmarker.processor.ContainerStatsProcessor;
import com.ivanfranchin.apiohabenchmarker.processor.DockerStatsProcessor;
import com.ivanfranchin.apiohabenchmarker.processor.OhaProcessor;
import com.ivanfranchin.apiohabenchmarker.processor.PodmanStatsProcessor;
import com.ivanfranchin.apiohabenchmarker.properties.AppContainerConfig;
import com.ivanfranchin.apiohabenchmarker.properties.AppType;
import com.ivanfranchin.apiohabenchmarker.properties.CadvisorProperties;
import com.ivanfranchin.apiohabenchmarker.properties.LoadTestRunnerProperties;
import com.ivanfranchin.apiohabenchmarker.properties.OhaParameter;
import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;
import com.ivanfranchin.apiohabenchmarker.writer.ResultFileWriter;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoadTestRunner implements CommandLineRunner {

  private final BrowserOpener browserOpener;
  private final OhaProcessor ohaProcessor;
  private final LoadTestRunnerProperties properties;
  private final CadvisorProperties cadvisorProperties;
  private final ResultFileWriter resultFileWriter;

  @Override
  public void run(String... args) {

    CadvisorContainer cadvisorContainer =
        cadvisorProperties.enabled() ? new CadvisorContainer() : null;
    try {
      if (cadvisorContainer != null) {
        cadvisorContainer.start();
      }

      Map<String, AppResult> appResultMap = new LinkedHashMap<>();
      for (Map.Entry<String, AppContainerConfig> entry : properties.getAppContainers().entrySet()) {
        String appContainerName = entry.getKey();
        AppContainerConfig config = entry.getValue();
        log.info("========== {} ==========", appContainerName);
        try (AppContainer appContainer =
            new AppContainer(appContainerName, config, properties.getContainerMemory().toBytes())) {
          appContainer.start();

          log.info("-----------------------------");
          log.info("Load testing {} with config {}", appContainerName, config);

          waitForContainerToStart();

          ContainerStatsProcessor containerStatsProcessor = createStatsProcessor(appContainerName);
          Thread containerStatsProcessorThread = new Thread(containerStatsProcessor);
          containerStatsProcessorThread.start();

          double startUpTime = getStartUpTime(appContainer, config.appType());
          log.info("StartUp time: {}s", startUpTime);

          if (cadvisorContainer != null && cadvisorProperties.openBrowser()) {
            browserOpener.open(appContainer.getContainerId(), cadvisorContainer.getHostPort());
          }

          List<LoadTestResult> loadTestResults = new ArrayList<>();
          for (OhaParameter ohaParameter : properties.getOhaParameters()) {
            int numRequests = ohaParameter.numRequests();
            int concurrency = ohaParameter.concurrency();
            String endpoint =
                ohaParameter.endpoint().startsWith("/")
                    ? ohaParameter.endpoint().substring(1)
                    : ohaParameter.endpoint();
            List<Double> ohaMetrics =
                ohaProcessor.run(numRequests, concurrency, appContainer.getHostPort(), endpoint);
            loadTestResults.add(new LoadTestResult(numRequests, concurrency, endpoint, ohaMetrics));

            pauseBetweenTests();
          }

          containerStatsProcessor.stop();
          try {
            containerStatsProcessorThread.join();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          }
          double maxCpuUsage = containerStatsProcessor.getMaxCpuUsage();
          double maxMemUsage = containerStatsProcessor.getMaxMemUsage();
          log.info("Max CPU usage:\t{}%", maxCpuUsage);
          log.info("Max memory usage:\t{}MB", maxMemUsage);

          appResultMap.put(
              appContainerName,
              new AppResult(startUpTime, maxCpuUsage, maxMemUsage, loadTestResults));
        }
      }
      resultFileWriter.write(appResultMap);
    } finally {
      if (cadvisorContainer != null) {
        cadvisorContainer.close();
      }
    }
  }

  private void waitForContainerToStart() {
    int millis = properties.getWaitForContainerToStartMillis();
    log.info("[ Waiting container to start. {} ms ]", millis);
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void pauseBetweenTests() {
    int millis = properties.getPauseBetweenTestsMillis();
    log.info("[ Pausing for {} ms ]", millis);
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private ContainerStatsProcessor createStatsProcessor(String containerName) {
    return switch (properties.getContainerRuntime()) {
      case DOCKER -> new DockerStatsProcessor(containerName);
      case PODMAN -> new PodmanStatsProcessor(containerName);
    };
  }

  private double getStartUpTime(AppContainer appContainer, AppType appType) {
    double value = appType.parseStartUpTime(appContainer.getLogs());
    if (value == -1.0) {
      log.error("Unable to get the startup time!");
    }
    return value;
  }
}
