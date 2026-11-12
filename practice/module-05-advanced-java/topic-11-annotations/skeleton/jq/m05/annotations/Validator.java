package jq.m05.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Validator {

    public static List<String> validate(Object settings) {
        List<String> messages = new ArrayList<>();
        // TODO 3: loop over settings.getClass().getDeclaredFields(), call setAccessible(true) and read each value
        // TODO 4: if the field has @NotBlank and the value is null or blank, add "<name> must not be blank"
        // TODO 5: if the field has @Range and the number is outside min to max, add "<name> must be between <min> and <max>"
        return messages;
    }

    public static void main(String[] args) {
        System.out.println(validate(new KioskSettings("Harbor", 12, "en")));
        System.out.println(validate(new KioskSettings(" ", 40, "en")));
    }
}
