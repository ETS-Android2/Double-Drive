package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Manager.Builder;

import org.junit.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import de.cronn.reflection.util.immutable.ImmutableProxy;
import static org.firstinspires.ftc.teamcode.AltFunctionality.*; //removes the need for AltFunctionality.[enum]


public class ManagerTest {
    Manager<ToMethod> funcDelayManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.PRINTDELAY)
            .addFunc(AltFunctionality.PRINTNODELAY) //notice how it also allows other enums
            .addParameterUnsafe(1)
            .build();
    Manager<ToMethod> funcErrManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.PRINTDELAY)
            .build();
    Manager<ToMethod> funcManManager = Manager.Builder.newBuilder()
            .addFunc(AltFunctionality.MANUALPRINT)
            .build();
    Manager<ToMethod> funcAndThen = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.PRINTDELAY)
            .addFunc(AltFunctionality.PRINTNODELAY)
            .addParameter(1)
            .build();
    Manager<ToMethod> funcExecWithConc = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.VRFUNCCONC)
            .addFunc(AltFunctionality.MANUALPRINT)
            .build();
    Manager<ToMethod> funcExecWithBlock = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.VRFUNCBLOCK)
            .addFunc(AltFunctionality.MANUALPRINT)
            .build();


    @Test
    public void funcDelay() {
        //Simple example displaying basic functionality
//        int i = 1;
                funcDelayManager.exec(TestClassEnum.PRINTDELAY) //Notice the parameter is automatically passed
                //^ This call has a 5000ms sleep. Because of the concurrency annotation, the below
                //  call will still proceed and will be completed first.
                .exec(AltFunctionality.PRINTNODELAY) //This is not delayed
                .await();
    }

    @Test
    public void funcError() {
        //The code below should throw an AssertionError. the parameter, while passed, is marked
        //as @Supplied@, therefore it will not expect to use that argument.
        Assert.assertThrows( () ->
            funcErrManager.exec(TestClassEnum.PRINTDELAY, 1)
        );
    }

    @Test
    public void funcManual() {
        funcManManager.exec(AltFunctionality.MANUALPRINT, 1337)
                .await();
    }

    @Test
    public void funcAndThen() {
        //This example displays one usage of execWith. In this case, it works effectively as an
        // "andThen()" by executing PRINTDELAY (delayed) THEN executing PRINTNODELAY. While that andThen
        // happens, it will automatically continue executing
        funcAndThen.execWith(TestClassEnum.PRINTDELAY, AltFunctionality.PRINTNODELAY)
                .exec(TestClassEnum.PRINTDELAY) //starts right after PRINTDELAY is initially called
                .await();

    }

    @Test
    public void funcExecWithConc() {
        funcExecWithConc.execWith(TestClassEnum.VRFUNCCONC, AltFunctionality.MANUALPRINT)
                .exec(AltFunctionality.MANUALPRINT, "Manual print after execWith()")
                //^ executed right after the execWith(), normally printing between them
                .await();
    }

    @Test
    public void funcExecWithBlock() {
        funcExecWithBlock.execWith(TestClassEnum.VRFUNCBLOCK, AltFunctionality.MANUALPRINT)
                .exec(AltFunctionality.MANUALPRINT, "Manual print after execWith()");
                //^ executed after the execWith() completes, normally printing after them.
    }
}
