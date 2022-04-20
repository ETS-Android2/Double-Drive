package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.lang.reflect.Method;

public enum LiftActions implements ToMethod {
    RAISELIFT, LOWERLIFT, GETLIFTLEVEL, CONTROLLER_CHECK;

    final static int pickup = 0;
    final static int carry  = 400;
    final static int drop_3 = 1400;


    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case LOWERLIFT: return this.getClass().getDeclaredMethod("lowerLift", BotConfig.class, LiftLevelI.class);
            case RAISELIFT: return this.getClass().getDeclaredMethod("raiseLift", BotConfig.class, LiftLevelI.class);
            case GETLIFTLEVEL: return this.getClass().getDeclaredMethod("getLiftLevel", BotConfig.class);
            case CONTROLLER_CHECK: return this.getClass().getDeclaredMethod("controllerCheck", Gamepad.class, String.class);
        }
        return null;
    }
    //////////////////////////////////////////////////////////////////////////
    @Concurrent(allowAsync = false)
    static void raiseLift(@Supplied BotConfig config, LiftLevelI lift) {
        if(!config.winchMotor.isBusy()) {
            LiftLevelI upLevel = lift.upMotorPos();
            config.winchMotor.setTargetPosition(upLevel.currMotorPos());
        }
    }

    @Concurrent(allowAsync = false)
    static void lowerLift(@Supplied BotConfig config, LiftLevelI lift) {
        if (!config.winchMotor.isBusy()) {
            LiftLevelI downLevel = lift.downMotorPos();
            config.winchMotor.setTargetPosition(downLevel.currMotorPos());
        }
    }

    @Concurrent(allowAsync = false)
    static LiftLevelI getLiftLevel(@Supplied BotConfig config) {
        int targetPos = config.winchMotor.getTargetPosition();
        return LiftActions.toLiftLevel(targetPos);
    }

    @Concurrent
    static boolean controllerCheck(Gamepad controller, String input) {
        switch(input.toLowerCase()) {
            case "a": return controller.a;
            case "b": return controller.b;
            default: return false;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    public static LiftLevelI toLiftLevel(int targetPos) throws IllegalArgumentException {
        switch(targetPos) {
            case pickup: return new LiftActions.Pickup();
            case carry:  return new LiftActions.Carry();
            case drop_3: return new LiftActions.Drop_3();
            default: throw new IllegalArgumentException();
        }
    }

    public static class Pickup implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new LiftActions.Carry();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new LiftActions.Pickup();
        }

        @Override
        public int currMotorPos() {
            return pickup;
        }

        public String toString() {
            return "Pickup";
        }
    }

    public static class Carry implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new LiftActions.Drop_3();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new LiftActions.Pickup();
        }

        @Override
        public int currMotorPos() {
            return carry;
        }

        public String toString() {
            return "Carry";
        }
    }

    public static class Drop_3 implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new LiftActions.Drop_3();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new LiftActions.Carry();
        }

        @Override
        public int currMotorPos() {
            return drop_3;
        }

        public String toString() {
            return "Drop 3";
        }
    }
}
