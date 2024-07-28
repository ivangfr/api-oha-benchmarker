package com.ivanfranchin.apiohabenchmarker.runner;

import com.ivanfranchin.apiohabenchmarker.browser.BrowserOpener;
import com.ivanfranchin.apiohabenchmarker.container.AppContainer;
import com.ivanfranchin.apiohabenchmarker.container.CadvisorContainer;
import com.ivanfranchin.apiohabenchmarker.processor.DockerStatsProcessor;
import com.ivanfranchin.apiohabenchmarker.processor.OhaProcessor;
import com.ivanfranchin.apiohabenchmarker.properties.AppContainerConfig;
import com.ivanfranchin.apiohabenchmarker.properties.AppType;
import com.ivanfranchin.apiohabenchmarker.properties.LoadTestRunnerProperties;
import com.ivanfranchin.apiohabenchmarker.result.AppResult;
import com.ivanfranchin.apiohabenchmarker.result.LoadTestResult;
import com.ivanfranchin.apiohabenchmarker.writer.ResultFileWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoadTestRunner implements CommandLineRunner {

    private final BrowserOpener browserOpener;
    private final OhaProcessor ohaProcessor;
    private final LoadTestRunnerProperties properties;

    @Override
    public void run(String... args) {

        try (CadvisorContainer cadvisorContainer = new CadvisorContainer()) {
            cadvisorContainer.start();

            Map<String, AppResult> appResultMap = new LinkedHashMap<>();
            for (String appContainerName : properties.getAppContainers().keySet()) {
                log.info("========== {} ==========", appContainerName);
                AppContainerConfig config = properties.getAppContainers().get(appContainerName);
                try (AppContainer appContainer = new AppContainer(appContainerName, config)) {
                    appContainer.start();

                    log.info("-----------------------------");
                    log.info("Load testing {} with config {}", appContainerName, config);

                    pause();

                    DockerStatsProcessor dockerStatsProcessor = new DockerStatsProcessor(appContainerName);
                    Thread dockerStatsProcessorThread = new Thread(dockerStatsProcessor);
                    dockerStatsProcessorThread.start();

                    double startUpTime = getStartUpTime(appContainer, config.appType());
                    log.info("StartUp time: {}s", startUpTime);

                    browserOpener.open(appContainer.getContainerId(), cadvisorContainer.getHostPort());

                    List<LoadTestResult> loadTestResults = new LinkedList<>();
                    for (String numReqsAndConcStr : properties.getNumRequestsAndConcurrency()) {
                        String[] numReqsAndConcArr = numReqsAndConcStr.split(":");
                        int numRequests = Integer.parseInt(numReqsAndConcArr[0]);
                        int concurrency = Integer.parseInt(numReqsAndConcArr[1]);
                        double[] ohaMetrics = ohaProcessor.run(
                                numRequests, concurrency, appContainer.getHostPort(), appContainer.getEndpoint());
                        loadTestResults.add(new LoadTestResult(numRequests, concurrency, ohaMetrics));

                        pause();
                    }

                    dockerStatsProcessor.stop();
                    double maxCpuUsage = dockerStatsProcessor.getMaxCpuUsage();
                    double maxMemUsage = dockerStatsProcessor.getMaxMemUsage();
                    log.info("Max CPU usage:\t{}%", maxCpuUsage);
                    log.info("Max memory usage:\t{}MB", maxMemUsage);

                    appResultMap.put(appContainerName, new AppResult(startUpTime, maxCpuUsage, maxMemUsage, loadTestResults));
                }
            }
            ResultFileWriter.write(appResultMap);
        }
    }

    private void pause() {
        int millis = properties.getPauseMillis();
        log.info("[ Pausing for {} ms ]", millis);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private double getStartUpTime(AppContainer appContainer, AppType appType) {
        String regex = "in ([\\d.]+) seconds";
        if (appType == AppType.QUARKUS) {
            regex = "started in ([\\d.]+)s";
        } else if (appType == AppType.MICRONAUT) {
            regex = "Startup completed in ([\\d.]+)ms";
        }
        Pattern pattern = Pattern.compile(regex);
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
