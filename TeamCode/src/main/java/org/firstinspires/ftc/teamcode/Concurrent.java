package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Concurrent {
    @NonNull ConcE behavior() default ConcE.CONCURRENT;
}
