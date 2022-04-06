package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

public enum AltFunctionality implements ToMethod {
    PRINTNODELAY, MANUALPRINT;

    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case PRINTNODELAY:   return ManagerTestFunctions.class.getDeclaredMethod("testPrint", Integer.class);
            case MANUALPRINT: return ManagerTestFunctions.class.getDeclaredMethod("testPrintManual", Integer.class);
            default: return null;
        }
    }
}
