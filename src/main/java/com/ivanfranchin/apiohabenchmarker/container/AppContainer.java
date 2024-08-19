package com.ivanfranchin.apiohabenchmarker.container;

import com.ivanfranchin.apiohabenchmarker.properties.AppContainerConfig;
import lombok.Getter;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Getter
public class AppContainer extends GenericContainer<AppContainer> {

    private static final long MAX_MEMORY = (long) 1024 * 1024 * 1024;
    private static final int DEFAULT_PORT = 8080;
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    private final int exposedPort;

    public AppContainer(String containerName, AppContainerConfig config) {
        super(DockerImageName.parse(config.dockerImageName()));
        this.exposedPort = config.exposedPort() == null ? DEFAULT_PORT : config.exposedPort();

        AppContainer container = withExposedPorts(exposedPort)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT))
                .withCreateContainerCmdModifier(cmd -> cmd
                        .withName(containerName)
                        .withHostConfig(cmd.getHostConfig().withMemory(MAX_MEMORY))
                );
        if (StringUtils.hasText(config.network())) {
            container.withNetwork(getNetworkByName(config.network()));
        }
        if (config.environment() != null) {
            for (String envStr : config.environment()) {
                String[] envArr = envStr.split("=");
                container.withEnv(envArr[0], envArr[1]);
            }
        }
    }

    public int getHostPort() {
        return this.getMappedPort(exposedPort);
    }

    private Network getNetworkByName(String networkName) {
        return new Network() {
            @Override
            public String getId() {
                return networkName;
            }

            @Override
            public void close() {
            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };
    }
}