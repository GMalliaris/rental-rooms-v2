package org.gmalliaris.rental.rooms.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null){
            return true;
        }
        var pattern = Pattern.compile("^\\+(?:\\d ?){6,14}\\d$");
        var matcher = pattern.matcher(s);

        return matcher.find() && matcher.group().equals(s);
    }
}
