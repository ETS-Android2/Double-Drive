package org.firstinspires.ftc.teamcode.actions;

import static java.lang.Thread.sleep;

import org.firstinspires.ftc.teamcode.BotConfig;
import org.firstinspires.ftc.teamcode.Concurrent;
import org.firstinspires.ftc.teamcode.LiftLevelI;
import org.firstinspires.ftc.teamcode.Supplied;
import org.firstinspires.ftc.teamcode.ToMethod;

import java.lang.reflect.Method;

public enum LiftActions implements ToMethod {
    RAISELIFT, LOWERLIFT, GETLIFTLEVEL, DROP, MANAGE_AUTO_LIFT_BEHAVIOR;

    final static private double basketDrop = 0;
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
            case MANAGE_AUTO_LIFT_BEHAVIOR: return this.getClass().getDeclaredMethod("manageAutoLiftBehavior", BotConfig.class);
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Concurrent
    public static void manageAutoLiftBehavior(@Supplied BotConfig robot) {
        LiftLevelI lift = LiftActions.getLiftLevel(robot);
        boolean isDropped  = robot.basket.getPosition() == basketDrop;
        boolean isDetected = GenericActions.checkCSensor(robot);

        if(lift instanceof Drop_3 && isDropped) {
            try {
                sleep(1500);
                isDropped = robot.basket.getPosition() == basketDrop; //re-compute, it may have changed
                if(!isDropped) return;
                lowerLift(robot); //this will lower the lift AND reset the servo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if(lift instanceof Pickup && isDetected) {
            raiseLift(robot);
        }
    }

    @Concurrent
    public static void drop(@Supplied BotConfig robot) {
        LiftLevelI lift = getLiftLevel(robot);
        if(lift instanceof Drop_3) {
            robot.basket.setPosition(basketDrop);
        }
    }

    @Concurrent//(allowAsync = false)
    public static void raiseLift(@Supplied BotConfig config) {
        LiftLevelI lift = getLiftLevel(config);
        if(!config.winchMotor.isBusy()) {
            LiftLevelI upLevel = lift.upMotorPos();
            config.winchMotor.setTargetPosition(upLevel.currMotorPos());
            config.basket.setPosition(upLevel.basketPos());
        }
    }

    @Concurrent//(allowAsync = false)
    public static void lowerLift(@Supplied BotConfig config) {
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
