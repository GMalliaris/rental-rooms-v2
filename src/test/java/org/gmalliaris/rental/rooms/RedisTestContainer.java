package org.gmalliaris.rental.rooms;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public interface RedisTestContainer {

    String REDIS_PASSWORD = "redis";
    DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:alpine3.16");

    @Container
    GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE)
            .withCommand("redis-server", "--requirepass", REDIS_PASSWORD)
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> "127.0.0.1");
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.redis.password", () -> REDIS_PASSWORD);
    }

}
