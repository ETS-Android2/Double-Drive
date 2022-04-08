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

    /**
     * {@code allowAsync = false} means a request (like {@code getLiftLevel()}) will finish before
     * another call to {@code getLiftLevel()} will proceed, but still allow for other operations
     * to continue, as the need for blocking pertains to this operation only.
     * @return Whether or not the method should allow async behavior. true by default, but should be
     * manually set for anything like a getter.
     */
    boolean allowAsync() default true;
}
