package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public enum TestClassEnum implements ToMethod {
    PRINTDELAY, VRFUNC;

    public Method toMethod() throws NoSuchMethodException {
        switch (this) {
            case PRINTDELAY:
                return ManagerTestFunctions.class.getDeclaredMethod("testPrintDelay", Integer.class);
            case VRFUNC:
                return ManagerTestFunctions.class.getDeclaredMethod("vrFunc");
            default:
                return null;
        }
    }
}
