package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

//ToMethod makes it easy to pass functions, represented as actions in the form of Enums, to a Manager.
public interface ToMethod<T> {
    Method toMethod(T type) throws NoSuchMethodException;
}
