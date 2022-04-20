package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

//FIXME: move non-lift actions to its own enum
public enum LiftActions implements ToMethod {
    RAISELIFT, LOWERLIFT, GETLIFTLEVEL, CONTROLLER_CHECK, PRINT_TO_TELEMETRY,
    DRIVE, DROP;

    final static int pickup = 0;
    final static int carry  = 400;
    final static int drop_3 = 1400;


    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case LOWERLIFT: return this.getClass().getDeclaredMethod("lowerLift", BotConfig.class);
            case RAISELIFT: return this.getClass().getDeclaredMethod("raiseLift", BotConfig.class);
            case GETLIFTLEVEL: return this.getClass().getDeclaredMethod("getLiftLevel", BotConfig.class);
            case CONTROLLER_CHECK: return this.getClass().getDeclaredMethod("controllerCheck", Gamepad.class, String.class);
            case PRINT_TO_TELEMETRY: return this.getClass().getDeclaredMethod("printToTelemetry", Telemetry.class, String.class);
            case DRIVE: return this.getClass().getDeclaredMethod("drive", BotConfig.class, Gamepad.class);
            case DROP: return this.getClass().getDeclaredMethod("drop", BotConfig.class);
        }
        return null;
    }
    //////////////////////////////////////////////////////////////////////////
    @Concurrent(behavior = ConcE.BLOCKING)
    static void printToTelemetry(Telemetry telemetry, String str) {
        telemetry.addData(str, "");
        telemetry.update();
    }

    @Concurrent
    static void drive(@Supplied BotConfig robot, Gamepad gamepad) {
        final double TOLERANCE = 0.1;

        double straif = -gamepad.left_stick_x;
        double forward = -gamepad.left_stick_y;
        float turnLeft = gamepad.left_trigger;
        float turnRight = gamepad.right_trigger;
        double angle_rad = robot.angles.firstAngle * -Math.PI/180.0;
        double turn = -turnLeft + turnRight;

        double temp = straif;
        straif = straif*Math.cos(angle_rad) - forward*Math.sin(angle_rad);
        forward = temp*Math.sin(angle_rad) + forward*Math.cos(angle_rad);

        // tolerance
        if (Math.abs(straif) <= TOLERANCE) {
            straif = 0;
        }
        if (Math.abs(forward) <= TOLERANCE) {
            forward = 0;
        }

        double m1D = (forward-straif+turn)/3.0;
        double m2D = (forward+straif+turn)/3.0;
        double m3D = (forward+straif-turn)/3.0;
        double m4D = (forward-straif-turn)/3.0;


        // set power for wheels
        robot.leftFront.setPower(m1D);
        robot.rightBack.setPower(m4D);
        robot.leftBack.setPower(m2D);
        robot.rightFront.setPower(m3D);
    }

    @Concurrent
    static void drop(@Supplied BotConfig robot) {
        LiftLevelI lift = getLiftLevel(robot);
        if(lift instanceof Drop_3) {
            robot.basket.setPosition(0);
        }
    }

    @Concurrent
    static boolean controllerCheck(Gamepad controller, String input) {
//        return true;
        switch(input.toLowerCase()) {
            case "a": return controller.a;
            case "b": return controller.b;
            case "x": return controller.x;
            case "y": return controller.y;
            case "idle" : return !controller.atRest();
            default: return false;
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
