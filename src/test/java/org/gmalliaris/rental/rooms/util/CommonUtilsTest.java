package org.gmalliaris.rental.rooms.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUtilsTest {

    @ParameterizedTest
    @MethodSource("provideUuidsAndResults")
    void uuidFromStringTest(String uuidString, Optional<UUID> expectedResult) {
        var result = CommonUtils.uuidFromString(uuidString);
        if (expectedResult.isPresent()) {
            assertTrue(result.isPresent());
            assertEquals(expectedResult.get(), result.get());
        }
        else {
            assertTrue(result.isEmpty());
        }
    }

    private static Stream<Arguments> provideUuidsAndResults() {
        var randomUUID = UUID.randomUUID();

        return Stream.of(Arguments.of(null, Optional.empty()),
                Arguments.of("blah", Optional.empty()),
                Arguments.of(randomUUID.toString(), Optional.of(randomUUID)));
    }
}