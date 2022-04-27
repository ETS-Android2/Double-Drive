package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

import java.util.concurrent.TimeUnit;

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

    @Concurrent (allowAsync = false)
    static <T> void testPrintManual(T i) {
        System.out.println("Received value: " + i);
    }

    @Concurrent
    static void strPrintManual(String str) {
        System.out.println(str);
    }

    @Concurrent
    static String vrFuncConc() {
        System.out.println("Delaying 2000ms...");
        try {
            sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "This string was concurrently passed from a value-returning function";
    }

    @Concurrent(behavior = ConcE.BLOCKING)
    static String vrFuncBlock() {
        System.out.println("Delaying 2000ms...");
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "This string was passed from a blocking value-returning function";
    }

    @Concurrent(behavior = ConcE.BLOCKING)
    static boolean returnTrueBlock() {
        return true;
    }

    @Concurrent(behavior = ConcE.BLOCKING)
    static boolean returnFalseBlock() {
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Concurrent
    static boolean returnFalseConc() {
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Concurrent
    static boolean returnTrueConc() {
        return true;
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

    @Concurrent(allowAsync = false)
    static LiftLevelI getLiftLevel(@Supplied RobotConfig config) {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int targetPos = config.winchMotor().getTargetPosition();
        return Levels.toLiftLevel(targetPos);
    }
    @Concurrent(allowAsync = false)
    static int getTargetPos(@Supplied RobotConfig config) {
        return config.winchMotor().getTargetPosition();
    }

    @Concurrent(
            allowAsync = false,
            ignoreOnAsync = true)
    static void ignoreNoAsyncPrintDelay(String str) {
        System.out.println(str);
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Concurrent
    @Scheduler(
            action = AltFunctionality.ADT.ManualPrint.class,
            args = {"man_print_schedule_str"},
            runAfter = 0L,
            unit = TimeUnit.SECONDS
    )
    static void emptyScheduler() {

    }
}
