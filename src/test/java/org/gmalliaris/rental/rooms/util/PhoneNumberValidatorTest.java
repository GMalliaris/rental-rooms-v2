package org.gmalliaris.rental.rooms.util;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PhoneNumberValidatorTest {

    private final PhoneNumberValidator validator = new PhoneNumberValidator();

    @Test
    void isValidTest(){
        var ph1 = "+30 6912345678";
        var ph2 = "+306912345678";
        var ph3 = "6912345678";
        var ph4 = "a";
        var ph5 = "69123456789";
        var ph6 = "691234567";
        var mockArg = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(ph1, mockArg));
        assertTrue(validator.isValid(ph2, mockArg));
        assertFalse(validator.isValid(ph3, mockArg));
        assertFalse(validator.isValid(ph4, mockArg));
        assertFalse(validator.isValid(ph5, mockArg));
        assertFalse(validator.isValid(ph6, mockArg));
    }
}