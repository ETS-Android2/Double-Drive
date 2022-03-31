package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Manager.Builder;

import org.junit.Test;
import org.testng.Assert;

//import static org.junit.Assert.assertThrows;

public class ManagerTest {
    Manager<ToMethod> addFuncDelayManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.UTILTEST)
            .addFunc(AltFunctionality.OTHERFUNC) //notice how it also allows other enums
            .addParameter(1)
            .build();

    Manager<ToMethod> addFuncErrManager = Manager.Builder.newBuilder()
            .addFunc(TestClassEnum.UTILTEST)
            .build();

    @Test
    public void addFuncDelay() {
        //Simple example displaying basic functionality
        int i = 1;
                addFuncDelayManager.exec(TestClassEnum.UTILTEST) //Notice the parameter is automatically passed
                //^ This call has a 5000ms sleep. Because of the concurrency annotation, the below
                //  call will still proceed and will be completed first.
                .exec(AltFunctionality.OTHERFUNC) //This is not delayed
                .await();

    }

    @Test
    public void addFuncError() {
        //The code below should throw an AssertionError. the parameter, while passed, is marked
        //as @Supplied@, therefore it will not expect to use that argument.
        Assert.assertThrows( () ->
            addFuncErrManager.exec(TestClassEnum.UTILTEST, 1)
        );
    }

}
