package org.usfirst.frc.team4737.robot.control;

import java.util.ArrayList;

import org.usfirst.frc.team4737.robot.Global;
import org.usfirst.frc.team4737.robot.Log;

/**
 * Uses functionalities of PID motion controllers and TMPs (Trapezoidal Motion Profilers) to create an all-inclusive
 * motion controller.<br>
 * <br>
 * Note: The differential term is currently not included.
 * 
 * @author Brian
 *
 */
public class MotionController {

	private static ArrayList<DataTable> saveable = new ArrayList<DataTable>();

	// Control values
	public double kP, kI;// , kD;
	public double accelLimit, velLimit, decelLimit;
	private DataTable accelTuning;

	private boolean mapping;
	private double lastValue;

	// Error calculation values
	private double goal, value;

	// Integral term handling
	private double integral;
	private double integralRange;

	// Differential term handling
	// private int recordLength;
	// private Vector2d[] velocityRecord;
	// private boolean validDiff = false;

	public MotionController(String id, double kP, double kI, /* double kD, */double accelLimit, double velLimit,
			double decelLimit, double expectedDeltaT, double integralRange, /* int differentialRecordingLength, */
			String accelTuningFile) {
		this.kP = kP;
		this.kI = kI;
		// this.kD = kD;
		this.accelLimit = accelLimit;
		this.velLimit = velLimit;
		this.decelLimit = decelLimit;

		if (accelTuningFile != null) {
			accelTuning = new DataTable(accelTuningFile);
		} else {
			accelTuning = new DataTable(.01, -1, 1, 0);
			mapping = true;
			Log.println("A data table had to be created! This table will be filled and saved when told to.");
			Log.println("\tData table save file: " + (id + ".txt") + ", press joystick 2:"
					+ Global.DATATABLE_SAVE_BUTTON + " to save");
			saveable.add(accelTuning);
		}

		this.integralRange = integralRange;

		// this.recordLength = differentialRecordingLength;
		// velocityRecord = new Vector2d[recordLength];
	}

	public void setGoal(double goal) {
		this.goal = goal;
	}

	public double getCurrentGoal() {
		return goal;
	}

	public double getLastValue() {
		return value;
	}

	public double getPower(double value, double acceleration, double velocity, double deltaT) {
		// Things to think about:
		// - integral integration before or after power calculation?
		// - same for differential

		// Mapping
		// Note: this will not be very accurate due to time delay between updates.
		// This could be made more accurate by calculating jerk
		if (mapping) {
			accelTuning.mapValue(lastValue, acceleration, false);
		}

		// Error
		this.value = value;
		double error = goal - value;

		// Integral calculation
		if (error < integralRange)
			integral += error * deltaT;

		// Differential calculation
		// for (int n = recordLength - 2; n > 0; n--) {
		// velocityRecord[n + 1] = velocityRecord[n];
		// }
		// velocityRecord[0] = new Vector2d(velocity, deltaT);
		// double differential = 0;
		// if (validDiff) {
		// double totalTime = 0, totalValue = 0;
		// for (int n = 0; n < recordLength; n++) {
		// totalValue += velocityRecord[n].x;
		// totalTime += velocityRecord[n].y;
		// }
		// differential = totalValue / totalTime;
		// } else {
		// // Checks if the record is full of values yet
		// validDiff = velocityRecord[recordLength - 1] != null;
		// }

		double pidPower = (kP * error) + (kI * integral);// + (kD * differential);

		double limitedPower = pidPower;

		// TMP power limiting

		if (velocity > velLimit) {
			double val = accelTuning.findClosestX((velLimit - velocity));
			if (val != Double.NaN)
				limitedPower = val;
		}
		if (velocity < -velLimit) {
			double val = accelTuning.findClosestX((velLimit - velocity));
			if (val != Double.NaN)
				limitedPower = -val;
		}

		if (acceleration > 0) {
			double val = accelTuning.getY(pidPower);
			if (val != Double.NaN)
				if (acceleration > accelLimit || val > accelLimit) {
					limitedPower = 0;
				}
		} else if (acceleration < 0) {
			double val = accelTuning.getY(pidPower);
			if (val != Double.NaN)
				if (-acceleration > decelLimit || -val > decelLimit) {
					limitedPower = accelTuning.findClosestX(decelLimit - acceleration);
				}
		}

		return limitedPower;
	}

}
