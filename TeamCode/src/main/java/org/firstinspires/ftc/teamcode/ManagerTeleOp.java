package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static org.firstinspires.ftc.teamcode.LiftActions.*;

@TeleOp(name="Manager Control Test", group="Pushbot")
public class ManagerTeleOp extends LinearOpMode {

    BotConfig robot = new BotConfig();
    Manager<ToMethod> manager = Manager.Builder.builder()
            .addFunc(RAISELIFT)
            .addFunc(LOWERLIFT)
            .addFunc(GETLIFTLEVEL)
            .addFunc(CONTROLLER_CHECK)
            .addParameterUnsafe(robot)
            .build();

    public void runOpMode() {
        robot.init(hardwareMap);

        manager .execIf(CONTROLLER_CHECK, mkArr(gamepad1, "a"), RAISELIFT)
                .execIf(CONTROLLER_CHECK, mkArr(gamepad1, "b"), LOWERLIFT);
    }

    @SafeVarargs //check for heap pollution (how?)
    private final <T> T[] mkArr(T... literals) {
        return literals;
    }
}
