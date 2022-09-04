package org.gmalliaris.rental.rooms.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(RedisConfigurationProperties.class)
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=9367",
        "spring.redis.password=redis"
})
class RedisConfigurationPropertiesTest {

    @Autowired
    private RedisConfigurationProperties redisConfigurationProperties;
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @MethodSource("provideHost")
    void redisHostTest(String value, int errorsSize) {
        var host = "host";
        ReflectionTestUtils.setField(redisConfigurationProperties, host, value);
        var errorsSet = validator.validateProperty(redisConfigurationProperties, host);
        assertEquals(errorsSize, errorsSet.size());
    }

    @ParameterizedTest
    @MethodSource("providePort")
    void redisPortTest(Integer value, int errorsSize) {
        var port = "port";
        ReflectionTestUtils.setField(redisConfigurationProperties, port, value);
        var errorsSet = validator.validateProperty(redisConfigurationProperties, port);
        assertEquals(errorsSize, errorsSet.size());
    }

    @ParameterizedTest
    @MethodSource("providePassword")
    void redisPasswordTest(String value, int errorsSize) {
        var password = "password";
        ReflectionTestUtils.setField(redisConfigurationProperties, password, value);
        var errorsSet = validator.validateProperty(redisConfigurationProperties, password);
        assertEquals(errorsSize, errorsSet.size());
    }

    private static Stream<Arguments> provideHost() {
        return Stream.of(Arguments.of(null, 1),
                Arguments.of("", 1),
                Arguments.of(" ", 1),
                Arguments.of("127.0.0.1", 0));
    }

    private static Stream<Arguments> providePort() {
        return Stream.of(Arguments.of(null, 1),
                Arguments.of(8080, 0));
    }

    private static Stream<Arguments> providePassword() {
        return Stream.of(Arguments.of(null, 1),
                Arguments.of("", 1),
                Arguments.of(" ", 1),
                Arguments.of("redis", 0));
    }
}