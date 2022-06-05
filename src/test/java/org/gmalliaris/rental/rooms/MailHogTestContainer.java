package org.gmalliaris.rental.rooms;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public interface MailHogTestContainer {

    DockerImageName MAILHOG_IMAGE = DockerImageName.parse("mailhog/mailhog:latest");

    @Container
    GenericContainer<?> MAILHOG_CONTAINER = new GenericContainer<>(MAILHOG_IMAGE)
            .withExposedPorts(1025, 8025);

    @DynamicPropertySource
    static void setMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> MAILHOG_CONTAINER.getMappedPort(1025));
    }

    default String getMailhogHttpUrl(){
        return String.format("http://%s:%s/api/v2/messages",
                MAILHOG_CONTAINER.getHost(), MAILHOG_CONTAINER.getMappedPort(8025));
    }

}
