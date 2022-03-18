package org.gmalliaris.rental.rooms.util;

import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        if (s == null || s.isBlank()){
            return true;
        }

        // Password must be at least 10 chars long
        var lengthRule = new LengthRule();
        lengthRule.setMinimumLength(10);

        // Password must contain at least one upper-case character
        var upperRule = new CharacterRule(EnglishCharacterData.UpperCase, 1);

        // Password must contain at least one lower-case character
        var lowerRule = new CharacterRule(EnglishCharacterData.LowerCase, 1);

        // Password must contain at least one digit character
        var digitRule = new CharacterRule(EnglishCharacterData.Digit, 1);

        // Password must contain at least one symbol (special character)
        var specialRule = new CharacterRule(EnglishCharacterData.Special, 1);

        var validator = new PasswordValidator(lengthRule, upperRule, lowerRule,
                digitRule, specialRule);

        var result = validator.validate(new PasswordData(s));

        return result.isValid();
    }
}
