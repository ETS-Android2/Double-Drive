package org.firstinspires.ftc.teamcode.actions;

import static org.firstinspires.ftc.teamcode.Logic.Lit;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BotConfig;
import org.firstinspires.ftc.teamcode.ConcE;
import org.firstinspires.ftc.teamcode.Concurrent;
import org.firstinspires.ftc.teamcode.LiftLevelI;
import org.firstinspires.ftc.teamcode.RobotUtils;
import org.firstinspires.ftc.teamcode.Logic;
import org.firstinspires.ftc.teamcode.Supplied;
import org.firstinspires.ftc.teamcode.ToCallable;
import org.firstinspires.ftc.teamcode.ToMethod;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.firstinspires.ftc.teamcode.actions.LiftActions.*;

//TODO: convert to an ADT instead of enum
public enum GenericActions implements ToMethod {
    CONTROLLER_CHECK, PRINT_TO_TELEMETRY, DRIVE, SPIN_ABDUCTOR, TURBO;

    @Override
    public Method toMethod() throws NoSuchMethodException {
       switch(this) {
           case CONTROLLER_CHECK: return this.getClass().getDeclaredMethod("controllerCheck", Gamepad.class, String.class);
           case PRINT_TO_TELEMETRY: return this.getClass().getDeclaredMethod("printToTelemetry", Telemetry.class, String.class);
           case DRIVE: return this.getClass().getDeclaredMethod("drive", BotConfig.class, Gamepad.class);
           case SPIN_ABDUCTOR: return this.getClass().getDeclaredMethod("spinAbductor", BotConfig.class, String.class);
           case TURBO: return this.getClass().getDeclaredMethod("turbo", BotConfig.class, String.class);
       }
       return null;
    }


    @Concurrent
    public static void spinAbductor(@Supplied BotConfig robot, String direction) {
        switch(direction.toLowerCase()) {
            case "cw"  : robot.abductor.setPower(0.7); break;
            case "ccw" : robot.abductor.setPower(-0.7); break;
            case "halt": robot.abductor.setPower(0); break;
        }
        if(Objects.isNull(direction)) {
            throw new IllegalArgumentException();
        }
    }

    @Concurrent
    public static void turbo(@Supplied BotConfig robot, String direction) {
        LiftLevelI lift = LiftActions.getLiftLevel(robot);
        if( lift instanceof LiftActions.Drop_3
         || lift instanceof LiftActions.Pickup) {
            return;
        }
        
        switch(direction) {
            case "fw":
                RobotUtils.setMotors(robot.leftBack, robot.leftFront, robot.rightBack, robot.rightFront,
                    1,1,1,1);
                break;
            case "bw":
                RobotUtils.setMotors(robot.leftBack, robot.leftFront, robot.rightBack, robot.rightFront,
                        -1,-1,-1,-1);
                break;
        }
    }

    @Concurrent(behavior = ConcE.BLOCKING)
    public static void printToTelemetry(Telemetry telemetry, String str) {
        telemetry.addData(str, "");
        telemetry.update();
    }

    @Concurrent
    public static void drive(@Supplied BotConfig robot, Gamepad gamepad) {
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
    public static boolean controllerCheck(Gamepad controller, String input) {
//        return true;
        switch(input.toLowerCase()) {
            case "a" : return controller.a;
            case "b" : return controller.b;
            case "x" : return controller.x;
            case "y" : return controller.y;
            case "lb": return controller.left_bumper;
            case "rb": return controller.right_bumper;
            case "idle" : return !controller.atRest();
            case "bump_not_held" : return !(controller.left_bumper || controller.right_bumper);
            default: return false;
        }
    }

    public static boolean checkCSensor(BotConfig robot) {
        NormalizedRGBA colors = robot.cSensor.getNormalizedColors();
        return colors.red > 0.004 && colors.green > 0.005;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generic Action Conditional. The convention for Conditionals and ADTs in this library is
     * the following:
     * <ul>
     *     <li>For Data Constructors (DCs) that hold data, parameter-less constructors ought to be
     *     private. </li>
     *     <li>For all DC Foo, there ought be a function Foo([data]) as shorthand for the constructor. </li>
     *     <li>For all DC Foo, there ought be a function FooL([data]) as shorthand for Logic construction.</li>
     *     <li>The {@code toCallable()} ought be morally pure. Any side effects performed should not break
     *     referential transparency if possible. e.g. {@code Print(String str)} is morally pure.</li>
     * </ul>
     */
    public abstract static class GenActCond implements ToCallable<Boolean> {
        public static class CheckController extends GenActCond implements ToCallable<Boolean> {
            private Gamepad gamepad;
            private String checkee;

            public CheckController(Gamepad gamepad, String checkee) {
                this.gamepad = gamepad;
                this.checkee = checkee;
            }

            private CheckController() {}

            @Override
            public Callable<Boolean> toCallable() {
                return () -> controllerCheck(gamepad, checkee);
            }
        }
        public static CheckController CheckController(Gamepad gamepad, String checkee) {
            return new CheckController(gamepad, checkee);
        }
        public static Logic<GenActCond> CheckControllerL(Gamepad gamepad, String checkee) {
            return Lit(CheckController(gamepad, checkee));
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        //Print is a debug GenAct, so it does not follow convention
        public static class Print extends GenActCond implements ToCallable<Boolean> {
            String str;

            public Print(String str) {
                this.str = str;
            }

            private Print() {}

            @Override
            public Callable<Boolean> toCallable() {
                return () -> {
                    System.out.println("This printed!!!!!");
                    return true;
                };
            }
        }
        public static Logic<GenActCond> PrintL(String str) {
            return Lit(new Print(str));
        }
    }
}
