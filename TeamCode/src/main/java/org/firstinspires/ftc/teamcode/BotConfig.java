package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all the specific hardware for a single robot.
 */
public class BotConfig
{
    /* Public OpMode members. */
    public DcMotor    leftFront  = null;
    public DcMotor    leftBack   = null;
    public DcMotor    rightFront = null;
    public DcMotor    rightBack  = null;
    public DcMotor    winchMotor = null;
    public DcMotor    abductor   = null; //continuous rotation
    public Servo      basket     = null;
    public BNO055IMU imu = null;
    public Orientation angles = null;
//    public WebcamName camera     = null;
//    public int cameraMonitor;
//    public String cameraName = null;


    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public BotConfig(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        imu = ahwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        hwMap = ahwMap;

        // Define and Initialize Motors
        leftFront  = hwMap.get(DcMotor.class, "FrontLeft");
        leftBack   = hwMap.get(DcMotor.class, "BackLeft");
        rightFront = hwMap.get(DcMotor.class, "FrontRight");
        rightBack  = hwMap.get(DcMotor.class, "BackRight");
        winchMotor = hwMap.get(DcMotor.class, "WinchMotor");
        abductor   = hwMap.get(DcMotor.class, "abductor");

        leftFront.setDirection (DcMotor.Direction.FORWARD);
        leftBack.setDirection  (DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection (DcMotor.Direction.REVERSE);
        winchMotor.setDirection(DcMotor.Direction.FORWARD);
        abductor.setDirection  (DcMotor.Direction.FORWARD);

        // Set all motors to zero power
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
        winchMotor.setPower(0);

        // Set all motors to run without encoders.
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        abductor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        abductor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        winchMotor.setTargetPosition(0);
        winchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        winchMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        winchMotor.setPower(0.7);
        // Define and initialize ALL installed servos.
        abductor = hwMap.get(DcMotor.class, "abductor");
        basket   = hwMap.get(Servo.class, "basket");

        // Define and initialize camera.
        //set abductor defaults
        abductor.setPower(0); //0.5 is stop for a continuous rotation servo
        // basket.setPosition(0.81);
    }
}
