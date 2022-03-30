package org.firstinspires.ftc.teamcode;

import java.lang.reflect.Method;

public enum TestClassEnum implements ToMethod<TestClassEnum> {
    UTILTEST;

    public Method toMethod(TestClassEnum type) throws NoSuchMethodException {
        switch (type) {
            case UTILTEST:
                return RobotUtils.class.getDeclaredMethod("testPrint", Integer.class);
            default:
                return null;
        }
    }
}
