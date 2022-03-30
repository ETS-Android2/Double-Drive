package org.firstinspires.ftc.teamcode;


public enum LiftLevel {
    PICKUP, CARRY, DROP_1, DROP_3;

    public static double level2Servo(LiftLevel level) {
        switch (level) {
            case PICKUP:
                return 0.81;
            case CARRY:
                return 0.55;
            case DROP_1: case DROP_3:
                return 0.3; //we dont want this to drop until the player
            //gives the drop command
        }
        return 0/0;
    }

    public static double level2Motor(LiftLevel level) {
        switch (level) {
            case PICKUP:
                return 0;
            case CARRY:
                return 403.275; //0.75*537.7
            case DROP_1:
                return 2688.5;//940.975; //1.75*537.7
            case DROP_3:
                return 2688.5;
        }
        return 0/0;
    }

    public static LiftLevel downLevel(LiftLevel level) {
        switch (level) {
            case PICKUP: case CARRY:
                return PICKUP;
            case DROP_1: case DROP_3:
                return CARRY;
        }
        // throw IllegalArgumentException;
        return level;
    }
    public static LiftLevel upLevel(LiftLevel level) {
        switch (level) {
            case PICKUP:
                return CARRY;
            case CARRY:
                return DROP_1;
            case DROP_1: case DROP_3:
                return DROP_3;
        }
        // throw IllegalArgumentException;
        return level;
    }
    public String toString() {
        switch (this) {
            case PICKUP:
                return "PICKUP";
            case CARRY:
                return "CARRY";
            case DROP_1:
                return "DROP_1";
            case DROP_3:
                return "DROP_3";
            default:
                return "bad value";
        }
    }
}