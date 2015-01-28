package org.usfirst.frc.team4737.robot.wrappers;

import edu.wpi.first.wpilibj.TalonSRX;

/**
 * A wrapper for the Victor including the ability to invert the output.
 * 
 * @author Brian
 *
 */
public class Motor {

	private TalonSRX talon;
	private boolean inverted;
	private double setValue;
	
	public Motor(int pin, boolean inverted) {
		talon = new TalonSRX(pin);
		this.inverted = inverted;
	}
	
	public void set(double power) {
		talon.set(inverted ? -power : power);
		setValue = power;
	}
	
	public double getSetValue() {
		return setValue;
	}
	
}
