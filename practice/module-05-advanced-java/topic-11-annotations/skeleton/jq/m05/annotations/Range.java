package jq.m05.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO 2: add @Retention(RetentionPolicy.RUNTIME) here too
@Target(ElementType.FIELD)
public @interface Range {

    int min() default 0;

    int max() default Integer.MAX_VALUE;
}
