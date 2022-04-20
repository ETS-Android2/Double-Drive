package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static org.firstinspires.ftc.teamcode.LiftActions.*;

import java.util.concurrent.Callable;

@TeleOp(name="Manager Control Test", group="Pushbot")
public class ManagerTeleOp extends LinearOpMode {
    private int ticker = 0;

    BotConfig robot = new BotConfig();

    Manager<ToMethod> manager = Manager.Builder.builder()
            .addFunc(RAISELIFT)
            .addFunc(LOWERLIFT)
            .addFunc(GETLIFTLEVEL)
            .addFunc(CONTROLLER_CHECK)
            .addFunc(PRINT_TO_TELEMETRY)
            .addFunc(DRIVE)
            .addFunc(DROP)
            .addParameterUnsafe(robot)
            .build();

    public void runOpMode() {
        robot.init(hardwareMap);

        waitForStart();
        while(opModeIsActive()) {
            telemetry.addData("gamepad1 a ", gamepad1.a); //this is debug info
            telemetry.addData("gamepad1 b ", gamepad1.b);
            telemetry.addData("ticker ", ticker);
            telemetry.update();
            ticker++;

            manager .exec(DRIVE, gamepad1)
                    .execIf(CONTROLLER_CHECK, mkArr(gamepad1, "a"), RAISELIFT)
                    .execIf(CONTROLLER_CHECK, mkArr(gamepad1, "b"), LOWERLIFT)
                    .execIf(CONTROLLER_CHECK, mkArr(gamepad1, "x"), DROP)
                    .exec(PRINT_TO_TELEMETRY, telemetry, "(DEBUG) This was manually printed")
                    .await();
        }
    }

    //FIXME: the second argument of execIf should release its semaphore
    //FIXME: errors are not thrown properly, NullPointerExceptions are thrown instead
    //FIXME: when manual args are not supplied, ArrayOutOfBoundsExceptions are thrown instead
    @SafeVarargs //check for heap pollution (how?)
    private final <T> T[] mkArr(T... literals) {
        return literals;
    }
}
