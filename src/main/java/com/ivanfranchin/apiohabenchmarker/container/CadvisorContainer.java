package com.ivanfranchin.apiohabenchmarker.container;

import com.github.dockerjava.api.model.Device;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class CadvisorContainer extends GenericContainer<CadvisorContainer> {

    private static final int CONTAINER_PORT = 8080;
    private static final String DOCKER_IMAGE_NAME = "gcr.io/cadvisor/cadvisor:v0.49.1";

    public CadvisorContainer() {
        super(DockerImageName.parse(DOCKER_IMAGE_NAME));
        withExposedPorts(CONTAINER_PORT);
        withCreateContainerCmdModifier(cmd -> cmd
                .withName("cAdvisor")
                .getHostConfig()
                .withPrivileged(true)
                .withDevices(Device.parse("/dev/kmsg")));
        withFileSystemBind("/", "/rootfs", BindMode.READ_ONLY);
        withFileSystemBind("/var/run", "/var/run", BindMode.READ_ONLY);
        withFileSystemBind("/sys", "/sys", BindMode.READ_ONLY);
        withFileSystemBind("/var/lib/docker/", "/var/lib/docker", BindMode.READ_ONLY);
        withFileSystemBind("/dev/disk/", "/dev/disk", BindMode.READ_ONLY);
        withFileSystemBind("/var/run/docker.sock", "/var/run/docker.sock", BindMode.READ_WRITE);
    }

    public int getHostPort() {
        return this.getMappedPort(CONTAINER_PORT);
    }
}