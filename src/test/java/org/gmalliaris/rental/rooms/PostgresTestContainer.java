package org.gmalliaris.rental.rooms;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public interface PostgresTestContainer {

    String DATA_JPA_TEST_JDBC_URL_PROPERTY = "spring.datasource.url=jdbc:tc:postgresql:14.3-alpine:///rental-rooms";
    DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:14.3-alpine");

    @Container
    PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("rental-rooms")
            .withExposedPorts(5432);

    @DynamicPropertySource
    static void setDatabaseRelatedProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }
}
