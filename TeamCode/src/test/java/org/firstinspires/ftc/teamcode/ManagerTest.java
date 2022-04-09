package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Manager.Builder;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.junit.Test;
import org.testng.Assert;
import de.cronn.reflection.util.immutable.ImmutableProxy;
import com.qualcomm.robotcore.eventloop.opmode.OpMode.*;

import static org.firstinspires.ftc.teamcode.TestClassEnum.*;
import static org.firstinspires.ftc.teamcode.AltFunctionality.*;


public class ManagerTest {

    @Test
    public void funcDelay() {
        Manager<ToMethod> funcDelayManager = Manager.Builder.builder()
                .addFunc(TestClassEnum.PRINTDELAY)
                .addFunc(AltFunctionality.PRINTNODELAY) //notice how it also allows other enums
                .addParameterUnsafe(1)
                .build();

        //Simple example displaying basic functionality
//        int i = 1;
                funcDelayManager.exec(TestClassEnum.PRINTDELAY) //Notice the parameter is automatically passed
                //^ This call has a 5000ms sleep. Because of the concurrency annotation, the below
                //  call will still proceed and will be completed first.
                .exec(AltFunctionality.PRINTNODELAY) //This is not delayed
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcError() {
        Manager<ToMethod> funcErrManager = Manager.Builder.builder()
                .addFunc(TestClassEnum.PRINTDELAY)
                .build();

        //The code below should throw an AssertionError. the parameter, while passed, is marked
        //as @Supplied@, therefore it will not expect to use that argument.
        Assert.assertThrows( () ->
            funcErrManager.exec(TestClassEnum.PRINTDELAY, 1)
        );
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcManual() {
        Manager<ToMethod> funcManManager = Manager.Builder.builder()
                .addFunc(AltFunctionality.MANUALPRINT)
                .build();

        funcManManager.exec(AltFunctionality.MANUALPRINT, "This is a manual argument")
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcAndThen() {
        Manager<ToMethod> funcAndThen = Manager.Builder.builder()
                .addFunc(TestClassEnum.PRINTDELAY)
                .addFunc(AltFunctionality.PRINTNODELAY)
                .addParameterUnsafe(1)
                .build();

        //This example displays one usage of execWith. In this case, it works effectively as an
        // "andThen()" by executing PRINTDELAY (delayed) THEN executing PRINTNODELAY. While that andThen
        // happens, it will automatically continue executing
        funcAndThen.execWith(TestClassEnum.PRINTDELAY, AltFunctionality.PRINTNODELAY)
                .exec(TestClassEnum.PRINTDELAY) //starts right after PRINTDELAY is initially called
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecWithConc() {
        Manager<ToMethod> funcExecWithConc = Manager.Builder.builder()
                .addFunc(TestClassEnum.VRFUNCCONC)
                .addFunc(AltFunctionality.MANUALPRINT)
                .build();

        funcExecWithConc.execWith(TestClassEnum.VRFUNCCONC, AltFunctionality.MANUALPRINT)
                .exec(AltFunctionality.MANUALPRINT, "Manual print after execWith()")
                //^ executed right after the execWith(), normally printing between them
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecWithBlock() {
        Manager<ToMethod> funcExecWithBlock = Manager.Builder.builder()
                .addFunc(TestClassEnum.VRFUNCBLOCK)
                .addFunc(AltFunctionality.MANUALPRINT)
                .build();

        funcExecWithBlock.execWith(TestClassEnum.VRFUNCBLOCK, AltFunctionality.MANUALPRINT)
                .exec(AltFunctionality.MANUALPRINT, "Manual print after execWith()");
                //^ executed after the execWith() completes, normally printing after them.
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcRaiseLift() {
        RobotConfig raiseLiftConfig = ImmutableRobotConfig.builder()
                .winchMotor(new FakeMotor())
                .build();
        Manager<ToMethod> funcRaiseLift = Manager.Builder.builder()
                .addFunc(AltFunctionality.RAISELIFT)
                .addFunc(AltFunctionality.LOWERLIFT)
                .addFunc(AltFunctionality.GETLIFTLEVEL) //get lift level is blocking
                .addFunc(AltFunctionality.MANUALPRINT)
                .addFunc(AltFunctionality.GETTARGETPOS)
                .addImmutableParameter(raiseLiftConfig, RobotConfig.class)
                .build();
        //TODO: THIS SHOULD ALLOW CONCURRENCY.
        //      Perhaps it would be possible to add to the annotations for @Concurrent.
        //      Such as waiting for a task marked as (allowAsyncAccess = false) when asked to run
        //      A task of the same value. This means the overall control flow would go on, but each
        //      request for that function would not be instantly filled
        funcRaiseLift
                .execManyWith(GETLIFTLEVEL, RAISELIFT, MANUALPRINT) //prints "Pickup"
                .execWith(GETLIFTLEVEL, MANUALPRINT) //prints "Carry"
                .exec(MANUALPRINT, "blah1") //this should print before "Carry"
                .execManyWith(GETLIFTLEVEL, RAISELIFT)
                .execWith(GETLIFTLEVEL, MANUALPRINT) //prints "Drop 3"
                .exec(MANUALPRINT, "blah2") //this should print before "Drop 3"
                .execManyWith(GETLIFTLEVEL, LOWERLIFT, LOWERLIFT)
                    //^ Note the semantics above. execManyWith() only passes the value once, so
                    //it doesn't change twice
                .execWith(GETLIFTLEVEL, MANUALPRINT) //prints "Carry"
                .execWith(GETTARGETPOS, MANUALPRINT) //prints "400"
                .await();

        //TODO: COMPLETE
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecManyWithConc() {
        Manager<ToMethod> funcExecManyWithConc = Manager.Builder.builder()
                .addFunc(AltFunctionality.MANUALPRINT)
                .addFunc(TestClassEnum.VRFUNCCONC)
                .build();

        funcExecManyWithConc
                .execManyWith(VRFUNCCONC, MANUALPRINT, MANUALPRINT)
                .exec(MANUALPRINT, "This is a manually passed string")
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecManyWithBlock() {
        Manager<ToMethod> funcExecManyWithBlock = Manager.Builder.builder()
                .addFunc(AltFunctionality.MANUALPRINT)
                .addFunc(TestClassEnum.VRFUNCBLOCK)
                .build();

        funcExecManyWithBlock
                .execManyWith(VRFUNCBLOCK, MANUALPRINT, MANUALPRINT)
                .exec(MANUALPRINT, "This is a manually passed string");
    }
}
