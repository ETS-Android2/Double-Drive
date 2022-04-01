package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

public class ManagerTestFunctions {

    @Concurrent
    static void testPrint(@Supplied Integer i) {
        System.out.println("this works!" +i);
    }

    @Concurrent //(
    //  behavior = ConcE.BLOCKING
    //)
    static void testPrintDelay(@Supplied Integer i) {
        System.out.println("Delaying 5000ms...");
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Delay finished: " + i);
    }

    @Concurrent
    static void testPrintManual(Integer i) {
        System.out.println("Received value: " + i);
    }

    @Concurrent
    static Integer vrFunc() {
        System.out.println("Delaying 2000ms...");
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 321;
    }
}
