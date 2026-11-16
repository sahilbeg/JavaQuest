package jq.m05.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO 1: add @Retention(RetentionPolicy.RUNTIME) so the Validator can still see this annotation while the program runs
@Target(ElementType.FIELD)
public @interface NotBlank {
}
