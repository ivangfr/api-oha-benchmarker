package com.ivanfranchin.apiohabenchmarker.runner;

import com.ivanfranchin.apiohabenchmarker.browser.BrowserOpener;
import com.ivanfranchin.apiohabenchmarker.container.AppContainer;
import com.ivanfranchin.apiohabenchmarker.container.CadvisorContainer;
import com.ivanfranchin.apiohabenchmarker.processor.DockerStatsProcessor;
import com.ivanfranchin.apiohabenchmarker.processor.OhaProcessor;
import com.ivanfranchin.apiohabenchmarker.properties.AppContainerConfig;
import com.ivanfranchin.apiohabenchmarker.properties.AppType;
import com.ivanfranchin.apiohabenchmarker.properties.CadvisorProperties;
import com.ivanfranchin.apiohabenchmarker.properties.LoadTestRunnerProperties;
import com.ivanfranchin.apiohabenchmarker.properties.OhaParameter;
import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;
import com.ivanfranchin.apiohabenchmarker.writer.ResultFileWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoadTestRunner implements CommandLineRunner {

    private static final Pattern PATTERN_SPRING_BOOT = Pattern.compile("in ([\\d.]+) seconds");
    private static final Pattern PATTERN_QUARKUS     = Pattern.compile("started in ([\\d.]+)s");
    private static final Pattern PATTERN_MICRONAUT   = Pattern.compile("Startup completed in ([\\d.]+)ms");

    private final BrowserOpener browserOpener;
    private final OhaProcessor ohaProcessor;
    private final LoadTestRunnerProperties properties;
    private final CadvisorProperties cadvisorProperties;
    private final ResultFileWriter resultFileWriter;

    @Override
    public void run(String... args) {

        CadvisorContainer cadvisorContainer = cadvisorProperties.enabled() ? new CadvisorContainer() : null;
        try {
            if (cadvisorContainer != null) {
                cadvisorContainer.start();
            }

            Map<String, AppResult> appResultMap = new LinkedHashMap<>();
            for (Map.Entry<String, AppContainerConfig> entry : properties.getAppContainers().entrySet()) {
                String appContainerName = entry.getKey();
                AppContainerConfig config = entry.getValue();
                log.info("========== {} ==========", appContainerName);
                try (AppContainer appContainer = new AppContainer(appContainerName, config)) {
                    appContainer.start();

                    log.info("-----------------------------");
                    log.info("Load testing {} with config {}", appContainerName, config);

                    waitForContainerToStart();

                    DockerStatsProcessor dockerStatsProcessor = new DockerStatsProcessor(appContainerName);
                    Thread dockerStatsProcessorThread = new Thread(dockerStatsProcessor);
                    dockerStatsProcessorThread.start();

                    double startUpTime = getStartUpTime(appContainer, config.appType());
                    log.info("StartUp time: {}s", startUpTime);

                    if (cadvisorContainer != null && cadvisorProperties.openBrowser()) {
                        browserOpener.open(appContainer.getContainerId(), cadvisorContainer.getHostPort());
                    }

                    List<LoadTestResult> loadTestResults = new ArrayList<>();
                    for (OhaParameter ohaParameter : properties.getOhaParameters()) {
                        int numRequests = ohaParameter.numRequests();
                        int concurrency = ohaParameter.concurrency();
                        String endpoint = ohaParameter.endpoint().startsWith("/") ?
                                ohaParameter.endpoint().substring(1) : ohaParameter.endpoint();
                        List<Double> ohaMetrics = ohaProcessor.run(numRequests, concurrency, appContainer.getHostPort(), endpoint);
                        loadTestResults.add(new LoadTestResult(numRequests, concurrency, endpoint, ohaMetrics));

                        pauseBetweenTests();
                    }

                    dockerStatsProcessor.stop();
                    try {
                        dockerStatsProcessorThread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    double maxCpuUsage = dockerStatsProcessor.getMaxCpuUsage();
                    double maxMemUsage = dockerStatsProcessor.getMaxMemUsage();
                    log.info("Max CPU usage:\t{}%", maxCpuUsage);
                    log.info("Max memory usage:\t{}MB", maxMemUsage);

                    appResultMap.put(appContainerName, new AppResult(startUpTime, maxCpuUsage, maxMemUsage, loadTestResults));
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

    private double getStartUpTime(AppContainer appContainer, AppType appType) {
        Pattern pattern = switch (appType) {
            case QUARKUS   -> PATTERN_QUARKUS;
            case MICRONAUT -> PATTERN_MICRONAUT;
            default        -> PATTERN_SPRING_BOOT;
        };
        Matcher matcher = pattern.matcher(appContainer.getLogs());
        double value = -1.0;
        if (matcher.find()) {
            value = Double.parseDouble(matcher.group(1));
            if (appType == AppType.MICRONAUT) {
                value /= 1000;
            }
        } else {
            log.error("Unable to get the startup time!");
        }
        return value;
    }
}
