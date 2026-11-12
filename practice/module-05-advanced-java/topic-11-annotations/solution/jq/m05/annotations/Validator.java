package jq.m05.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Validator {

    public static List<String> validate(Object settings) {
        List<String> messages = new ArrayList<>();
        for (Field field : settings.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(settings);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read " + field.getName(), e);
            }

            if (field.isAnnotationPresent(NotBlank.class)) {
                if (value == null || value.toString().isBlank()) {
                    messages.add(field.getName() + " must not be blank");
                }
            }

            Range range = field.getAnnotation(Range.class);
            if (range != null && value instanceof Integer number) {
                if (number < range.min() || number > range.max()) {
                    messages.add(field.getName() + " must be between " + range.min() + " and " + range.max());
                }
            }
        }
        return messages;
    }

    public static void main(String[] args) {
        System.out.println(validate(new KioskSettings("Harbor", 12, "en")));
        System.out.println(validate(new KioskSettings(" ", 40, "en")));
    }
}
