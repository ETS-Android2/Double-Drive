package org.firstinspires.ftc.teamcode;

import org.junit.Test;

public class ManagerTest {
    Manager<TestClassEnum> manager = new Manager<>();


    @Test
    public void addFunc() {
//        System.out.println("blah");
        int i = 1;
        manager.addFunc(TestClassEnum.UTILTEST)
                .addParameter(i)
                .exec(TestClassEnum.UTILTEST);

    }

}
