package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

public class Levels {
    final static int pickup = 0;
    final static int carry  = 400;
    final static int drop_3 = 1400;

    public static LiftLevelI toLiftLevel(int targetPos) throws IllegalArgumentException {
        switch(targetPos) {
            case pickup: return new Pickup();
            case carry:  return new Carry();
            case drop_3: return new Drop_3();
            default: throw new IllegalArgumentException();
        }
    }

    public static class Pickup implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new Carry();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new Pickup();
        }

        @Override
        public int currMotorPos() {
            return pickup;
        }

        public String toString() {
            return "Pickup";
        }
    }

    public static class Carry implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new Drop_3();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new Pickup();
        }

        @Override
        public int currMotorPos() {
            return carry;
        }

        public String toString() {
            return "Carry";
        }
    }

    public static class Drop_3 implements LiftLevelI {
        @Override
        public LiftLevelI upMotorPos() {
            return new Drop_3();
        }

        @Override
        public LiftLevelI downMotorPos() {
            return new Carry();
        }

        @Override
        public int currMotorPos() {
            return drop_3;
        }

        public String toString() {
            return "Drop 3";
        }
    }

}
