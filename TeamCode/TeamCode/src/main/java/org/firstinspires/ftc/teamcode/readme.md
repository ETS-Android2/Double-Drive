# Java Like It's Hot (16553)'s code (2021-)

## Design Decisions and Code Logic
The design philosophy by this code is to be individually testable. Functions are not expected to
mutate state unless required, but mutating the internal state of passed objects is expected. This
allows the code to be easily testable, but cannot be reinforced by the Type System. 
Perhaps we will use Scala next year?

## Robot Specifications
The code supports a robot with the following features:
- Linear slide with an encoder
- Servo to rotate carrying mechanism
- Servo to spin ducky platform
- Four wheel holonomic drive
- Webcam for autonomous navigation -- TODO: IMPLEMENT
- Rev Robotics IMU                 -- TODO: IMPLEMENT
- Blinkin' LED Controller for runtime driver information -- TODO: IMPLEMENT


## Acknowledgements
FTC - Base library for controlling the robot