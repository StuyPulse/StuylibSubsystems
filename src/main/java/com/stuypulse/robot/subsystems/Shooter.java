package com.stuypulse.robot.subsystems;

import static com.stuypulse.robot.constants.Settings.Shooter.*;

import com.stuypulse.robot.util.Motor;
import com.stuypulse.stuylib.control.Controller;
import com.stuypulse.stuylib.control.feedback.*;
import com.stuypulse.stuylib.control.feedforward.*;
import com.stuypulse.stuylib.network.SmartNumber;
import com.stuypulse.stuylib.streams.filters.*;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * An example shooter subsystem where a motor is controlled by feedforward
 * and PID given a target velocity to run at. 
 * 
 * Using the controller class, a PID feedback controller and a motor feedforward are composed 
 * together. In addition, the target velocity is filtered using StuyLib's filtering library.
 * 
 * This is an example of velocity control, where the setpoints and measurements will be in 
 * units of velocity. 
 */
public class Shooter extends SubsystemBase {

    /** The controller, which will encapsulate feedforward, feedback, and setpoint/measurement/output filters. */
    private final Controller controller;
    
    /** The setpoint that the controller will adjust the shooter RPM to. */
    private Number setpoint;

    /** The motor being controlled at a velocity. */
    private final Motor motor;

    public Shooter() {
        /**
         * Combines both PID Controller and flywheel Feed Forward together.
         * 
         * The controller additionally allows the setpoints (and measurement / outputs) to be filtered.
         * Here there is a RateLimit, which sets a maximum change in target rpm over a second.
         * There is also a LowPassFilter, which smooths out the Target RPM to prevent the RPM from constantly changing.
         */
        controller = new PIDController(1.0, 0.0, 0.0)
            .and(new Feedforward.Flywheel(0.1, 0.01, 0.01).velocity())
            .setSetpointFilter(new RateLimit(1000).then(new LowPassFilter(0.2)));

        motor = new Motor();
        setpoint = new SmartNumber("Shooter/Setpoint (rpm)", 0.0);
    }

    @Override
    public void periodic() {
        if (controller.isDone(MAX_ERROR.doubleValue())) {
            /** If the controller is within an acceptable error, we can stop the motor. */
            motor.stop();
        } else {
            /** Calculates the next output of the controller. */
            double output = controller.update(setpoint.doubleValue(), motor.getVelocity());
            motor.set(output);
        }
        
        /** Displays the current Motor RPM onto SmartDashboard with filtering */
        SmartDashboard.putNumber("Shooter/Measurement (rpm)", controller.getMeasurement());
        // NOTE: it is best to read the measurement from the controller because there may be filters applied 
    }
}