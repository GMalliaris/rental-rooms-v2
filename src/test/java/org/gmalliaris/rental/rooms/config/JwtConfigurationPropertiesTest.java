package org.gmalliaris.rental.rooms.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(JwtConfigurationProperties.class)
class JwtConfigurationPropertiesTest {

    @Autowired
    private JwtConfigurationProperties jwtConfigurationProperties;
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaultValuesTest() {
        assertEquals(120, jwtConfigurationProperties.getAccessExpirationSeconds());
        assertEquals(60, jwtConfigurationProperties.getRefreshExpirationMinutes());
    }

    @ParameterizedTest
    @MethodSource("provideAccessExpirationSeconds")
    void accessExpirationSecondsTest(int value, int errorsSize) {
        String accessExpirationSeconds = "accessExpirationSeconds";
        ReflectionTestUtils.setField(jwtConfigurationProperties, accessExpirationSeconds, value);
        var errorSet = validator.validateProperty(jwtConfigurationProperties, accessExpirationSeconds);
        assertEquals(errorsSize, errorSet.size());
    }

    @ParameterizedTest
    @MethodSource("provideRefreshExpirationMinutes")
    void refreshExpirationMinutesTest(int value, int errorsSize) {
        String refreshExpirationMinutes = "refreshExpirationMinutes";
        ReflectionTestUtils.setField(jwtConfigurationProperties, refreshExpirationMinutes, value);
        var errorSet = validator.validateProperty(jwtConfigurationProperties, refreshExpirationMinutes);
        assertEquals(errorsSize, errorSet.size());
    }

    private static Stream<Arguments> provideAccessExpirationSeconds() {
        return Stream.of(Arguments.of(119, 1),
                Arguments.of(120, 0),
                Arguments.of(300, 0),
                Arguments.of(301, 1));
    }

    private static Stream<Arguments> provideRefreshExpirationMinutes() {
        return Stream.of(Arguments.of(14, 1),
                Arguments.of(15, 0),
                Arguments.of(60, 0),
                Arguments.of(61, 1));
    }
}