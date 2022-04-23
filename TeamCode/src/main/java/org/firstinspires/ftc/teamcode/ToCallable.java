package org.firstinspires.ftc.teamcode;

import java.util.concurrent.Callable;

public interface ToCallable<T> {
    Callable<T> toCallable();
}
