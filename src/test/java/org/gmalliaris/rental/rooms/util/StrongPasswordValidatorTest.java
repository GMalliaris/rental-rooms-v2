package org.gmalliaris.rental.rooms.util;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void isValidTest(){
        var pwd1 = "1234567aA!";
        // no special char
        var pwd2 = "1234567aA0";
        // no lowe-case char
        var pwd3 = "1234567BA!";
        // no upper-case char
        var pwd4 = "1234567ab!";
        // no digit
        var pwd5 = "aaaaaaaB!@";
        // under 10 characters
        var pwd6 = "123456aA!";
        var blank = "  ";

        var mockArg = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(pwd1, mockArg));
        assertFalse(validator.isValid(pwd2, mockArg));
        assertFalse(validator.isValid(pwd3, mockArg));
        assertFalse(validator.isValid(pwd4, mockArg));
        assertFalse(validator.isValid(pwd5, mockArg));
        assertFalse(validator.isValid(pwd6, mockArg));
        assertTrue(validator.isValid(blank, mockArg));
        assertTrue(validator.isValid(null, mockArg));
    }
}