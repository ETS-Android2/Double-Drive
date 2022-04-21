package org.firstinspires.ftc.teamcode;


import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import java.util.logging.Level;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.Locale;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;

import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


public class RobotUtils {
    //TODO: ADD JAVADOC COMMENTS
    public static int blah = 0;
    synchronized static void moveLiftDown(LiftLevel level, Servo servo, DcMotor motor) {
        // double servPos = LiftLevel.level2Val(level);
        //get the values we need to go to from the current level
        LiftLevel downPos = LiftLevel.downLevel(level);

        double toPosS = LiftLevel.level2Servo(downPos);
        int toPosM    = (int)LiftLevel.level2Motor(downPos);

        double motorPos = motor.getCurrentPosition();
        //getCurrentPosition() gives a value in encoder ticks.
        //there are 537.7 encoder ticks per revolution
        try {
        switch (level) { //PICKUP, CARRY, DROP_1, DROP_3
            case PICKUP:
                break;
            case CARRY:
                motor.setTargetPosition(toPosM);
                sleep(100);
                servo.setPosition(toPosS);

                break;
            case DROP_1: case DROP_3:
                servo.setPosition(toPosS);
                motor.setTargetPosition(toPosM); //1.75 * 537.7

                break;

        }
        }
        catch (InterruptedException ignored){}
        //     int rotTarget = robot.winchMotor.getCurrentPosition() + (int)(3.75);
    }
    synchronized static void moveLiftUp(LiftLevel level, Servo servo, DcMotor motor) {
        // double servPos = LiftLevel.level2Val(level);
        //get the values we need to go to from the current level
        LiftLevel downPos = LiftLevel.upLevel(level);

        double toPosS = LiftLevel.level2Servo(downPos);
        int toPosM    = (int)LiftLevel.level2Motor(downPos);

        double motorPos = motor.getCurrentPosition();
        //getCurrentPosition() gives a value in encoder ticks.
        //there are 537.7 encoder ticks per revolution
        switch (level) { //PICKUP, CARRY, DROP_1, DROP_3
            case PICKUP:
                servo.setPosition(toPosS);
                motor.setTargetPosition(toPosM);
                break;
            case CARRY:
            case DROP_1:   //1.75 * 537.7
            case DROP_3:
                motor.setTargetPosition(toPosM);
                servo.setPosition(toPosS);
                break;
        }
    }

    public static void setMotors(DcMotor leftF, DcMotor leftB, DcMotor rightF, DcMotor rightB,
                   double m1,     double m2,     double m3,      double m4) {
        leftF .setPower(m1);
        leftB .setPower(m2);
        rightF.setPower(m3);
        rightB.setPower(m4);
    }
    public static void stopMotors(DcMotor leftF,  DcMotor leftB,
                    DcMotor rightF, DcMotor rightB) {
        leftF .setPower(0);
        leftB .setPower(0);
        rightF.setPower(0);
        rightB.setPower(0);
    }

}
