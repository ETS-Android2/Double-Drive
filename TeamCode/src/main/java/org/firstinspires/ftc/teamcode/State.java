package org.firstinspires.ftc.teamcode;



//TODO
public class State {
    private RobotUtils lift;

    public RobotUtils getLift() {
        return lift;
    }

    public void setLift(int lift) {
        RobotUtils.blah = lift;
    }

    public State() {
        lift = new RobotUtils();
    }
}
