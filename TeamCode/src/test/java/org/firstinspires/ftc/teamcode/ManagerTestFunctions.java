package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

public class ManagerTestFunctions {

    @Concurrent
    static void testPrint(@Supplied Integer i) {
        System.out.println("this works!" +i);
    }

    @Concurrent
    static void testPrintDelay(@Supplied Integer i) {
        System.out.println("Delaying 5000ms...");
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Delay finished: " + i);
    }

    @Concurrent
    static <T> void testPrintManual(T i) {
        System.out.println("Received value: " + i);
    }

    @Concurrent
    static Integer vrFuncConc() {
        System.out.println("Delaying 2000ms...");
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 321;
    }

    @Concurrent(behavior = ConcE.BLOCKING)
    static Integer vrFuncBlock() {
        System.out.println("Delaying 2000ms...");
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 321;
    }


    //LIFT TESTING FUNCTIONS
    @Concurrent
    static void raiseLift(@Supplied RobotConfig config, LiftLevelI lift) {
        LiftLevelI upLevel = lift.upMotorPos();
        config.winchMotor().setTargetPosition(upLevel.currMotorPos());
    }
    @Concurrent
    static void lowerLift(@Supplied RobotConfig config, LiftLevelI lift) {
        LiftLevelI downLevel = lift.downMotorPos();
        config.winchMotor().setTargetPosition(downLevel.currMotorPos());
    }

    @Concurrent
    static LiftLevelI getLiftLevel(@Supplied RobotConfig config) {
        int targetPos = config.winchMotor().getTargetPosition();
        return Levels.toLiftLevel(targetPos);
    }
}
