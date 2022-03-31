package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

//ToMethod makes it easy to pass functions, represented as actions in the form of Enums, to a Manager.
public interface ToMethod {
    Method toMethod() throws NoSuchMethodException;
}
