package org.firstinspires.ftc.teamcode.actions;

import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BotConfig;
import org.firstinspires.ftc.teamcode.ConcE;
import org.firstinspires.ftc.teamcode.Concurrent;
import org.firstinspires.ftc.teamcode.LiftLevelI;
import org.firstinspires.ftc.teamcode.Supplied;
import org.firstinspires.ftc.teamcode.ToMethod;

import java.lang.reflect.Method;

public enum LiftActions implements ToMethod {
    RAISELIFT, LOWERLIFT, GETLIFTLEVEL, DROP;

    final static private int pickup = 0;
    final static private int carry  = 400;
    final static private int drop_3 = 1400;


    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case LOWERLIFT: return this.getClass().getDeclaredMethod("lowerLift", BotConfig.class);
            case RAISELIFT: return this.getClass().getDeclaredMethod("raiseLift", BotConfig.class);
            case GETLIFTLEVEL: return this.getClass().getDeclaredMethod("getLiftLevel", BotConfig.class);
            case DROP: return this.getClass().getDeclaredMethod("drop", BotConfig.class);
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Concurrent
    static void drop(@Supplied BotConfig robot) {
        LiftLevelI lift = getLiftLevel(robot);
        if(lift instanceof Drop_3) {
            robot.basket.setPosition(0);
        }
    }


    @Concurrent//(allowAsync = false)
    static void raiseLift(@Supplied BotConfig config) {
        LiftLevelI lift = getLiftLevel(config);
        if(!config.winchMotor.isBusy()) {
            LiftLevelI upLevel = lift.upMotorPos();
            config.winchMotor.setTargetPosition(upLevel.currMotorPos());
            config.basket.setPosition(upLevel.basketPos());
        }
    }

    @Concurrent//(allowAsync = false)
    static void lowerLift(@Supplied BotConfig config) {
        LiftLevelI lift = getLiftLevel(config);
        if (!config.winchMotor.isBusy()) {
            LiftLevelI downLevel = lift.downMotorPos();
            config.winchMotor.setTargetPosition(downLevel.currMotorPos());
            config.basket.setPosition(downLevel.basketPos());
        }
    }

    @Concurrent(allowAsync = false)
    static LiftLevelI getLiftLevel(@Supplied BotConfig config) {
        int targetPos = config.winchMotor.getTargetPosition();
        return LiftActions.toLiftLevel(targetPos);
    }

    ////////////////////////////////////////   ADTs   //////////////////////////////////////////////

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

        @Override
        public double basketPos() {
            return 0.81;
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

        @Override
        public double basketPos() {
            return 0.55;
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

        @Override
        public double basketPos() {
            return 0.3;
        }

        public String toString() {
            return "Drop 3";
        }
    }
}
