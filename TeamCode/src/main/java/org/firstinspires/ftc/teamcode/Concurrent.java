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
     * manually set for anything like a getter. {@code allowAsync} has no effect when
     * {@code behavior} is false. Essentially, it is blocking among itself and concurrent among others.
     *
     * @implNote Due to limitations in the code, it doesn't seem too feasible to allow the concurrency
     * to continue until the next blocking call. For example, assume function {@code F} blocks forever.
     * <pre>
     * manager.execWith(F, foo)
     *        .exec(bar)
     *        .exec(foobar)
     *        .execWith(F, func)
     *        .exec(a)
     *        .exec(b)
     *        .await();
     * </pre>
     * In this snippet, {@code bar} and {@code foobar} would be executed, but {@code a} and {@code b}
     * will not. This is due to the implementation using semaphores and blocking. The only other way
     * I see this being possible is via some form of queuing in some synchronized array which can be flushed,
     * but that complicates the user API.
     */
    boolean allowAsync() default true;
}
