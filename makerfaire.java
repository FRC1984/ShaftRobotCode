//@author Matt Plotas ^-_-^
//For use in Aerial Assist game, 2013-2014
//season including KC Regional, Maker Faire,
//Ozark Mountain Brawl, MO State Fair, and
//Cowtown Throwdown.

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class makerfaire extends SimpleRobot {
    private Jaguar leftMotor;
    private Jaguar rightMotor;
    private Victor pickupArm;
    private DoubleSolenoid shooter;
    //Actually is two seperate single solenoids, just a lot 
    //cleaner to control this way.
    private DoubleSolenoid pickupPiston;
    private Joystick driveJoystick;
    private Compressor compressor;
    private Gyro gyro;
    //Gyro angle variable.
    private int angle;
    
    public void robotInit() {
        //Both Jags and Victors are runnning two per
        //PWM output with wye cables.
        leftMotor     = new Jaguar(1);
        rightMotor    = new Jaguar(3);
        pickupArm     = new Victor(5);
        shooter       = new DoubleSolenoid(1,2);
        pickupPiston  = new DoubleSolenoid(3,4);
        driveJoystick = new Joystick(1);
        compressor    = new Compressor(1, 1);
        gyro          = new Gyro(1);
        System.out.println("Robot initialized.");
    }
    public void autonomous() {
        compressor.start();
        System.out.println("Autonomous called");
        Timer auto = new Timer();
        auto.start();
        pickupPiston.set(DoubleSolenoid.Value.kForward);
        Timer.delay(1);
        pickupPiston.set(DoubleSolenoid.Value.kReverse);
        // Drive straight w/gyro correction.
        while(auto.get()<6){
            angle = (int)((1.083*gyro.getAngle())%360.0);
            int correct = angle / 500;
            leftMotor.set(.35+correct);
            rightMotor.set(-.35);
        }
        //There's a reason I didn't just call shoot(); here,
        //but I don't remember. Will test.
        leftMotor.set(0);
        rightMotor.set(0);        
        Timer.delay(1);
        pickupPiston.set(DoubleSolenoid.Value.kForward);
        Timer.delay(.5);
        shooter.set(DoubleSolenoid.Value.kForward);
        Timer.delay(.5);
        shooter.set(DoubleSolenoid.Value.kReverse);
        compressor.stop();
    }   
    public void operatorControl() {
        System.out.println("Operator control started; compressor started.");
        compressor.start();
        while (isOperatorControl() && isEnabled()) {
            double joystickX = driveJoystick.getAxis(Joystick.AxisType.kX);
            double joystickY = driveJoystick.getAxis(Joystick.AxisType.kY);
            double rightSpeed, leftSpeed;
            //Magical equation to assign motor values based on joystick.
            leftSpeed = joystickY + joystickX;
            rightSpeed = -joystickY + joystickX;
            leftMotor.set(leftSpeed);
            rightMotor.set(rightSpeed); 
            //Pickup/eject code. pressing down will put the arms down and 
            //spin intake, releasing puts arms back up and stops them. Pressing
            //up only runs motors backwards, does not move arm.
            if (driveJoystick.getRawButton(2) ) {
                pickupPiston.set(DoubleSolenoid.Value.kForward);
                pickupArm.set(-.55);     
            } else if (driveJoystick.getRawButton(3)){
                pickupArm.set(.55);
            } else {
                pickupPiston.set(DoubleSolenoid.Value.kReverse);
                pickupArm.set(0);
            } 
            //Shooting sequence.
            if (driveJoystick.getRawButton(1)) {
               shoot();
            } else {
                shooter.set(DoubleSolenoid.Value.kReverse);
            }
            //Allleviates CPU overload.
            Timer.delay(0.01);
            //Sets current heading as 0, used to auto align to that
            //angle later.
            if (driveJoystick.getRawButton(7) && driveJoystick.getRawButton(10)){
                gyro.reset();  
            }
            //Auto aligns to angle 0. Cancel with 5.
            if (driveJoystick.getRawButton(8)) 
            {
                align();
            }
            // Reformat gyro output for easier reading.
            // The 1.042 is to correct the deviation 
            // that occurs when the gyro goes more than
            // one full rotation in one direction.
            angle = (int)((1.042*gyro.getAngle())%360.0);
            SmartDashboard.putNumber("Gyro:  ", angle);
            SmartDashboard.putNumber("Rate:   ", (gyro.getRate()));
            SmartDashboard.putBoolean("Compressor:   ", compressor.enabled());
        }
    }
    public void test() {
            System.out.println("Test called, NYI");
    }
    public void disabled() {
        leftMotor.set(0);
        rightMotor.set(0);
        // Will the compressor run during disabled
        // if I comment this out? Need to try sometime.
        compressor.stop();
        System.out.println("Robot disabled; compressor stopped.");
    }
    public void shoot() {
         leftMotor.set(0);
         rightMotor.set(0);
         pickupPiston.set(DoubleSolenoid.Value.kForward);
         Timer.delay(.5);
         shooter.set(DoubleSolenoid.Value.kForward);
         Timer.delay(.5);
         shooter.set(DoubleSolenoid.Value.kReverse);
         pickupPiston.set(DoubleSolenoid.Value.kReverse);
         Timer.delay(.8);
         pickupPiston.set(DoubleSolenoid.Value.kForward);
         Timer.delay(.5);
         pickupPiston.set(DoubleSolenoid.Value.kReverse);
    }
    public void align() {
         boolean aligned = false;
         while (!aligned && !driveJoystick.getRawButton(5))
         {
             angle = (int)((1.042*gyro.getAngle())%360.0);
             if (angle < -4)    {
                 rightMotor.set(.35);
                 leftMotor.set(.35);   
             } else if (angle > 4)   {
                 leftMotor.set(-.35);
                 rightMotor.set(-.35);
             } else {   
                    // stop, pause and resample to compensate coasting/overshooting       
                    leftMotor.set(0);
                    rightMotor.set(0);
                    Timer.delay(.25);
                    angle = (int)((1.083*gyro.getAngle())%360.0);
                    if (angle < 4 && angle > -4) {
                        aligned = true;
                    }   
             }
         }       
    }
}
