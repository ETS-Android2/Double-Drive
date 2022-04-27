package org.firstinspires.ftc.teamcode;


import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public enum AltFunctionality implements ToMethod {
    PRINTNODELAY, MANUALPRINT, RAISELIFT, LOWERLIFT, GETLIFTLEVEL,
    GETTARGETPOS, RETURNTRUE_B, RETURNFALSE_B, RETURNFALSE_C, RETURNTRUE_C,
    IGNORED_NO_ASYNC_PRINT_DELAY, EMPTY_SCHEDULER;

    @Override
    public Method toMethod() throws NoSuchMethodException {
        switch(this) {
            case PRINTNODELAY:  return ManagerTestFunctions.class.getDeclaredMethod("testPrint", Integer.class);
            case MANUALPRINT:   return ManagerTestFunctions.class.getDeclaredMethod("testPrintManual", Object.class);
            case RAISELIFT:     return ManagerTestFunctions.class.getDeclaredMethod("raiseLift", RobotConfig.class, LiftLevelI.class);
            case LOWERLIFT:     return ManagerTestFunctions.class.getDeclaredMethod("lowerLift", RobotConfig.class, LiftLevelI.class);
            case GETLIFTLEVEL:  return ManagerTestFunctions.class.getDeclaredMethod("getLiftLevel", RobotConfig.class);
            case GETTARGETPOS:  return ManagerTestFunctions.class.getDeclaredMethod("getTargetPos", RobotConfig.class);
            case RETURNFALSE_B: return ManagerTestFunctions.class.getDeclaredMethod("returnFalseBlock");
            case RETURNTRUE_B:  return ManagerTestFunctions.class.getDeclaredMethod("returnTrueBlock");
            case RETURNFALSE_C: return ManagerTestFunctions.class.getDeclaredMethod("returnFalseConc");
            case RETURNTRUE_C:  return ManagerTestFunctions.class.getDeclaredMethod("returnTrueConc");
            case IGNORED_NO_ASYNC_PRINT_DELAY: return ManagerTestFunctions.class.getDeclaredMethod("ignoreNoAsyncPrintDelay", String.class);
            case EMPTY_SCHEDULER: return ManagerTestFunctions.class.getDeclaredMethod("emptyScheduler");
            default: return null;
        }
    }

    /**
     * Conditionals is for tests involving Logic
     */
    public abstract static class Conditionals implements ToCallable<Boolean> {
        public static class RetTrueCond extends Conditionals implements ToCallable<Boolean>{

            @Override
            public Callable<Boolean> toCallable() {
                return () -> true;
            }
        }
        public static RetTrueCond RetTrueCond() {
            return new RetTrueCond();
        }
        /////////////////////////////////////////////////////////
        public static class RetFalseCond extends Conditionals implements ToCallable<Boolean> {

            @Override
            public Callable<Boolean> toCallable() {
                return () -> false;
            }
        }
        public static RetFalseCond RetFalseCond() {
            return new RetFalseCond();
        }
        //////////////////////////////////////////////////////////
        public static class NoCompileCond extends Conditionals implements ToCallable<Boolean> {

//            @Override          |- ERROR!
//            public Callable<Integer> toCallable() {
//                return null;
//            }

            @Override
            public Callable<Boolean> toCallable() {
                return null;
            }
        }
        public static NoCompileCond NoCompileCond() {
            return new NoCompileCond();
        }
        //////////////////////////////////////////////////////////
    }

    public abstract static class ADT implements ToMethod {
        public static class ManualPrint extends ADT implements ToMethod {

            private static final ManualPrint self = new ManualPrint();

            public ManualPrint() {}

            public static ManualPrint getADT() {
                return self;
            }

            @Override
            public Method toMethod() throws NoSuchMethodException {
                return ManagerTestFunctions.class.getDeclaredMethod("strPrintManual", String.class);
            }
        }
        public static ManualPrint ManualPrint() {
            return ManualPrint.getADT();
        }
    }
}
