package org.firstinspires.ftc.teamcode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * A Scheduler annotation means an Action is scheduled to run some amount of time after the Action it
 * annotates completes. <strong>These will always be run concurrently to avoid messy control flow. </strong>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scheduler {
    /**
     * The action to schedule. It must be in your Manager as a function. Note that this requires a
     * Class, which means enums will not work well with this. Convert it to an ADT, then pass the
     * Class that represents the Action you want to run.
     */
    Class<? extends ToMethod> action();

    /**
     * This represents the arguments passed to the Method gained from {@code action}. To use these,
     * the Strings must be in your Manager as auto arguments. N.B. these do not have Type conflicts,
     * only name conflicts with other String-referenced auto arguments. Auto arguments by type will
     * still be passed like usual. This is necessarily clunky, as annotations cannot contain arbitrary
     * Objects.
     */
    String[] args();

    /**
     * How many units of time until the action is executed.
     */
    long runAfter();

    /**
     * The unit of time which describes {@code runAfter}.
     */
    TimeUnit unit();
}
