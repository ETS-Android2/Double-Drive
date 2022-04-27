package org.firstinspires.ftc.teamcode.actions;


import org.firstinspires.ftc.teamcode.BotConfig;
import org.firstinspires.ftc.teamcode.ConcE;
import org.firstinspires.ftc.teamcode.Concurrent;
import org.firstinspires.ftc.teamcode.LiftLevelI;
import org.firstinspires.ftc.teamcode.Manager;
import org.firstinspires.ftc.teamcode.Scheduler;
import org.firstinspires.ftc.teamcode.Supplied;
import org.firstinspires.ftc.teamcode.ToMethod;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public enum LiftActions implements ToMethod {
    RAISELIFT, LOWERLIFT, GETLIFTLEVEL, DROP;

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
//            case MANAGE_AUTO_LIFT_BEHAVIOR: return this.getClass().getDeclaredMethod("manageAutoLiftBehavior", BotConfig.class);
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Concurrent(
            allowAsync = false,
            ignoreOnAsync = true
    )
    //FIXME: implement scheduled delays via concurrency annotations so that this can run automatically
    //       after Drop is run, splitting this into two functions
    public static void manageAutoLiftReturn(@Supplied BotConfig robot) {
        LiftLevelI lift = LiftActions.getLiftLevel(robot);
        boolean isDropped  = robot.basket.getPosition() == basketDrop;

        if(lift instanceof Drop_3 && isDropped) {
            lowerLift(robot); //this will lower the lift AND reset the servo
        }
    }

    @Concurrent
    @Scheduler(
        action = LiftADT.Manage_Auto_Lift_Return.class,
        runAfter = 2500,
        unit = TimeUnit.MILLISECONDS,
        args = {}
    )
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

    public static abstract class LiftADT implements ToMethod {
        public static class Manage_Auto_Lift_Return extends LiftADT implements ToMethod {
            static final Manage_Auto_Lift_Return self = new Manage_Auto_Lift_Return();

            static Manage_Auto_Lift_Return getADT() {
                return self;
            }

            @Override
            public Method toMethod() throws NoSuchMethodException {
                return LiftActions.class.getDeclaredMethod("manageAutoLiftReturn", BotConfig.class);
            }
        }
        public static Manage_Auto_Lift_Return Manage_Auto_Lift_Return() {
            return Manage_Auto_Lift_Return.getADT();
        }
    }
}
