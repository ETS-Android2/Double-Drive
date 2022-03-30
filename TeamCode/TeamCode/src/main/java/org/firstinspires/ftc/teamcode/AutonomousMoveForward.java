package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import java.util.logging.Level;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.Locale;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

@Autonomous

public class AutonomousMoveForward extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();
    BotConfig robot = new BotConfig();
    final double TOLERANCE = 0.1;

    // The IMU sensor object
    BNO055IMU imu;
    Orientation angles;

    @Override
    public void runOpMode() {

        // initialize hardware variables
        robot.init(hardwareMap);
        Object lock = new Object();
        double straif;
        double forward;
        double turnLeft;
        double turnRight;
        LiftLevel lift = LiftLevel.PICKUP;

        // Set up the parameters with which we will use our IMU. Note that integration
        // algorithm here just reports accelerations to the logcat log; it doesn't actually
        // provide positional information.
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit            = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit            = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile  = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled       = true;
        parameters.loggingTag           = "IMU";

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Hello Driver");    //
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        //reset encoder
        robot.winchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.winchMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        telemetry.addData("Winch motor ",  "Starting at %7d",
                robot.winchMotor.getCurrentPosition());
        telemetry.update();
        DcMotor lf = robot.leftFront;
        DcMotor lb = robot.leftBack;
        DcMotor rf = robot.rightFront;
        DcMotor rb = robot.rightBack;

        final double pwr = 0.3;
        final int rotTime = 1580;
        final int duckyTime = 3000;
        final int f1 = 1000;
        final int s1 = 3000;
        final int f2 = 1000;


        while (opModeIsActive()) {
            // todo: write your code here
            setMotors(lf, lb, rf, rb, pwr,pwr,pwr,pwr);
            sleep(2000);
            stopMotors(lf,lb,rf,rb);
            return;
        }
    }
    void setMotors(DcMotor leftF, DcMotor leftB, DcMotor rightF, DcMotor rightB,
                   double m1,     double m2,     double m3,      double m4) {
        leftF .setPower(m1);
        leftB .setPower(m2);
        rightF.setPower(m3);
        rightB.setPower(m4);
    }
    void stopMotors(DcMotor leftF,  DcMotor leftB,
                    DcMotor rightF, DcMotor rightB) {
        leftF .setPower(0);
        leftB .setPower(0);
        rightF.setPower(0);
        rightB.setPower(0);
    }
}