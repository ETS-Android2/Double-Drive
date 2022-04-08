package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

public enum AltFunctionality implements ToMethod {
    PRINTNODELAY, MANUALPRINT, RAISELIFT, LOWERLIFT, GETLIFTLEVEL,
    GETTARGETPOS;

    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case PRINTNODELAY: return ManagerTestFunctions.class.getDeclaredMethod("testPrint", Integer.class);
            case MANUALPRINT:  return ManagerTestFunctions.class.getDeclaredMethod("testPrintManual", Object.class);
            case RAISELIFT:    return ManagerTestFunctions.class.getDeclaredMethod("raiseLift", RobotConfig.class, LiftLevelI.class);
            case LOWERLIFT:    return ManagerTestFunctions.class.getDeclaredMethod("lowerLift", RobotConfig.class, LiftLevelI.class);
            case GETLIFTLEVEL: return ManagerTestFunctions.class.getDeclaredMethod("getLiftLevel", RobotConfig.class);
            case GETTARGETPOS: return ManagerTestFunctions.class.getDeclaredMethod("getTargetPos", RobotConfig.class);
            default: return null;
        }
    }
}
