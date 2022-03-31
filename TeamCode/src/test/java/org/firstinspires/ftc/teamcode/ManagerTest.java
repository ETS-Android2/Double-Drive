package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Manager.Builder;

import org.junit.Test;
import org.testng.Assert;

//import static org.junit.Assert.assertThrows;

public class ManagerTest {
    Manager<ToMethod> funcDelayManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.UTILTEST)
            .addFunc(AltFunctionality.OTHERFUNC) //notice how it also allows other enums
            .addParameter(1)
            .build();

    Manager<ToMethod> funcErrManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.UTILTEST)
            .build();
    Manager<ToMethod> funcManManager = Manager.Builder.newBuilder()
            .addFunc(AltFunctionality.MANUALPRINT)
            .build();

    @Test
    public void funcDelay() {
        //Simple example displaying basic functionality
        int i = 1;
                funcDelayManager.exec(TestClassEnum.UTILTEST) //Notice the parameter is automatically passed
                //^ This call has a 5000ms sleep. Because of the concurrency annotation, the below
                //  call will still proceed and will be completed first.
                .exec(AltFunctionality.OTHERFUNC) //This is not delayed
                .await();

    }

    @Test
    public void funcError() {
        //The code below should throw an AssertionError. the parameter, while passed, is marked
        //as @Supplied@, therefore it will not expect to use that argument.
        Assert.assertThrows( () ->
            funcErrManager.exec(TestClassEnum.UTILTEST, 1)
        );
    }

    @Test
    public void funcManual() {
        funcManManager.exec(AltFunctionality.MANUALPRINT, 1337)
                .await();
    }

}
