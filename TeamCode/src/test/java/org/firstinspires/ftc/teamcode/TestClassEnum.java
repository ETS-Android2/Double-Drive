package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public enum TestClassEnum implements ToMethod {
    UTILTEST;

    public Method toMethod() throws NoSuchMethodException {
        switch (this) {
            case UTILTEST:
                return RobotUtils.class.getDeclaredMethod("testPrintDelay", Integer.class);
            default:
                return null;
        }
    }
}
