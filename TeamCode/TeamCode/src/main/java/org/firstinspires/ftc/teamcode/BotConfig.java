package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

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
    public Servo      abductor   = null; //continuous rotation
    public Servo      basket     = null;
    public WebcamName camera     = null;
    public int cameraMonitor;
    public String cameraName = null;


    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public BotConfig(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftFront  = hwMap.get(DcMotor.class, "FrontLeft");
        leftBack   = hwMap.get(DcMotor.class, "BackLeft");
        rightFront = hwMap.get(DcMotor.class, "FrontRight");
        rightBack  = hwMap.get(DcMotor.class, "BackRight");
        winchMotor = hwMap.get(DcMotor.class, "WinchMotor");

        leftFront.setDirection (DcMotor.Direction.FORWARD);
        leftBack.setDirection  (DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection (DcMotor.Direction.REVERSE);
        winchMotor.setDirection(DcMotor.Direction.FORWARD);

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

        winchMotor.setTargetPosition(0);
        winchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        winchMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        winchMotor.setPower(0.2);
        // Define and initialize ALL installed servos.
        abductor = hwMap.get(Servo.class, "abductor");
        basket   = hwMap.get(Servo.class, "basket");

        // Define and initialize camera.
        cameraName = "webcam";
        camera = hwMap.get(WebcamName.class, cameraName);
        // Define and initialize camera monitor.
        // TODO: Ensure this works
        cameraMonitor = hwMap
                .appContext
                .getResources()
                .getIdentifier("cameraMintorViewId", "id", hwMap.appContext.getPackageName());


        //set abductor defaults
        abductor.setPosition(0.5); //0.5 is stop for a continuous rotation servo
        // basket.setPosition(0.81);
    }
}
