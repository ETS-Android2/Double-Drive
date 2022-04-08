package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.immutables.value.Value;

import java.lang.annotation.Retention;

/**
 * This is an example interface for a RobotConfig, which should be created ad-hoc according to robot
 * specifications. The Immutable annotation gives access to a nice builder, so it is preferred. It
 * also allows us to use addParameterUnsafe() because we know it is immutable
 */
@Value.Immutable
public interface RobotConfig {

//    DcMotor frontLeft();
//    DcMotor backLeft();
//    DcMotor frontRight();
//    DcMotor backRight();
    DcMotor winchMotor();
//    Servo   basket();
}
