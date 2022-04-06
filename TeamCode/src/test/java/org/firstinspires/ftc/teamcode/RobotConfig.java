package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.immutables.value.Value;

/**
 * This is an example interface for a RobotConfig, which should be created ad-hoc according to robot
 * specifications.
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
