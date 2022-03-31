package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

public enum AltFunctionality implements ToMethod {
    OTHERFUNC;

    @Override
    public Method toMethod() throws NoSuchMethodException {
        return RobotUtils.class.getDeclaredMethod("testPrint", Integer.class);
    }
}
