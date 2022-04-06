package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public enum TestClassEnum implements ToMethod {
    PRINTDELAY, VRFUNCCONC, VRFUNCBLOCK;

    public Method toMethod() throws NoSuchMethodException {
        switch (this) {
            case PRINTDELAY:
                return ManagerTestFunctions.class.getDeclaredMethod("testPrintDelay", Integer.class);
            case VRFUNCCONC:
                return ManagerTestFunctions.class.getDeclaredMethod("vrFuncConc");
            case VRFUNCBLOCK:
                return ManagerTestFunctions.class.getDeclaredMethod("vrFuncBlock");
            default:
                return null;
        }
    }
}
