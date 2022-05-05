package org.firstinspires.ftc.teamcode;

import org.junit.Test;
import org.testng.Assert;

import static org.firstinspires.ftc.teamcode.ManagerTestFunctions.ignoreNoAsyncPrintDelay;
import static org.firstinspires.ftc.teamcode.TestClassEnum.*;
import static org.firstinspires.ftc.teamcode.AltFunctionality.*;
import static org.firstinspires.ftc.teamcode.AltFunctionality.Conditionals.*;
import static org.firstinspires.ftc.teamcode.AltFunctionality.ADT.*;
import static org.firstinspires.ftc.teamcode.actions.GenericActions.GenActCond.*;
import static org.firstinspires.ftc.teamcode.Logic.*;



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
        funcDelayManager
                .exec(TestClassEnum.PRINTDELAY) //Notice the parameter is automatically passed
                //^ This call has a 5000ms sleep. Because of the concurrency annotation, the below
                //  call will still proceed and will be completed first.
                .exec(AltFunctionality.PRINTNODELAY) //This is not delayed
                .await();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
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
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecIfBlock() {
        Manager<ToMethod> funcExecIfBlock = Manager.Builder.builder()
                .addFunc(RETURNFALSE_B)
                .addFunc(RETURNTRUE_B)
                .addFunc(MANUALPRINT)
                .build();

        funcExecIfBlock
                .execIf(RETURNFALSE_B, MANUALPRINT) //shouldn't even print, but should delay
                .execIf(RETURNTRUE_B, MANUALPRINT); //prints true. execIf passes true if it succeeds
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcExecIfConc() {
        Manager<ToMethod> funcExecIfConc = Manager.Builder.builder()
                .addFunc(RETURNFALSE_C)
                .addFunc(RETURNTRUE_C)
                .addFunc(MANUALPRINT)
                .build();

        funcExecIfConc
                .execIf(RETURNFALSE_C, MANUALPRINT) //shouldn't even print, but should delay
                .execIf(RETURNTRUE_C, MANUALPRINT) //prints true. execIf passes true if it succeeds
                .await();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcLogicCallableBool() {
        Logic<Boolean> logic =
                And(
                        Or(
                            Lit(true),
                            Lit(false)
                        ),
                        Lit(true)
                );
        boolean result = false;
        try {
                result = toCallableBools(logic).call();
        } catch(Exception e) { e.printStackTrace(); }

        System.out.println(result);
        Assert.assertTrue(result, "AST did not evaluate to True");
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcLogicCallable() {
        Logic<Conditionals> logic =
            And(
                Or(
                    Lit(RetTrueCond()),
                    Lit(RetFalseCond())
                ),
                Lit(RetTrueCond())
            );
        boolean result = false;
        try {
            result = toCallable(logic).call();
        } catch(Exception e) { e.printStackTrace(); }
        System.out.println(result);
        Assert.assertTrue(result, "AST did not evaluate to True");
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcLogicManager() {
        Manager<ToMethod> manager = Manager.Builder.builder()
                .addFunc(MANUALPRINT)
                .build();

        manager .execIf(PrintL("This printed!!!!"), MANUALPRINT)
                .await();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcIgnoreAsync() {
        Manager<ToMethod> manager = Manager.Builder.builder()
                .addFunc(IGNORED_NO_ASYNC_PRINT_DELAY)
                .addFunc(MANUALPRINT)
                .build();

        for(int i=0; i<100; i++) {
            manager.exec(IGNORED_NO_ASYNC_PRINT_DELAY, "Printing: "+i);
//                   .exec(MANUALPRINT, "This is a manual print. Currently i = "+i);
            if(i%100==0) {
                manager.displayPoolStatistics();
            }
        }
        manager.await();
        //About the result: this should only print once, first with the "printing" then the "delay."
        //The purpose of this is so when something that delays (not recommended) or takes a while is
        //run concurrently, can't be run concurrently with itself, and mustn't block the manager, it
        //can be ignored and not flood the thread pool.
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void funcSchedulerTest() {
        Manager<ToMethod> manager = Manager.Builder.builder()
                .addFunc(ManualPrint())
                .addFunc(EMPTY_SCHEDULER)
                .addStrParameter("man_print_schedule_str", "Got value from strParameter!")
                .build();

        manager .exec(EMPTY_SCHEDULER)
                .await();
//        try {
//            sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
