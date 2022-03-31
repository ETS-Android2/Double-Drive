package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

public enum AltFunctionality implements ToMethod {
    OTHERFUNC, MANUALPRINT;

    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case OTHERFUNC:   return RobotUtils.class.getDeclaredMethod("testPrint", Integer.class);
            case MANUALPRINT: return RobotUtils.class.getDeclaredMethod("testPrintManual", Integer.class);
            default: return null;
        }
    }
}
