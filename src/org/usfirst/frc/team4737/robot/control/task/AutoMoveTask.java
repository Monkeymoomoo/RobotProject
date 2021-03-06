package org.usfirst.frc.team4737.robot.control.task;

import org.usfirst.frc.team4737.robot.Global;
import org.usfirst.frc.team4737.robot.Robot;
import org.usfirst.frc.team4737.robot.math.Vector2d;

public class AutoMoveTask extends AbstractRobotTask {

	private boolean global;
	private boolean usePid;
	private Vector2d locationOrig;
	private Vector2d goal;
	
	/**
	 * An automatic movement task that runs for a maximum of 30 seconds.
	 * 
	 * @param goal
	 *            - The coordinates to move to
	 * @param globalCoords
	 *            - Whether or not the coordinates are global (relative to robot
	 *            init location/angle) or relative (relative to robot's
	 *            location/angle in task initialization).
	 */
	public AutoMoveTask(Vector2d goal, boolean globalCoords, boolean useDistancePid) {
		super("autonMoveTask", 30);
		this.global = globalCoords;
		this.usePid = useDistancePid;
	}

	public void init(Robot robot) {
		locationOrig = new Vector2d(robot.position.position.x, robot.position.position.y);
		if (global) {
		} else {
			goal = goal.plus(new Vector2d(robot.position.gyroAngle.z + Math.toDegrees(Math.atan2(goal.y, goal.x)), Math.hypot(goal.x, goal.y), locationOrig));
		}
	}

	public void periodicExecution(Robot robot) {
		double angleCurrent = robot.position.gyroAngle.z;
		Vector2d locationCurrent = new Vector2d(robot.position.position.x, robot.position.position.y);
		
		double angleGoal = Math.toDegrees(Math.atan2(goal.y - locationCurrent.y, goal.x - locationCurrent.y));
		double distanceGoal = 0.1;
		
		double angleError = angleGoal - (angleCurrent % 360);
		double distanceError = Math.max(0, distanceGoal - goal.distance(locationCurrent));
		
		// Using arcade drive code
		double xAxis = angleError * Global.AUTOMOVE_ANGULAR_kP; // Rotational axis
		double yAxis = usePid ? Math.min(distanceError * Global.AUTOMOVE_kP, Global.AUTOMOVE_MAXSPEED) : Global.AUTOMOVE_MAXSPEED; // Magnitude axis
		double zAxis = 1; // Scale axis

		double leftSpeedMod = Math.min(Math.abs(-1 / Global.ARCADE_YAW_SENSITIVITY - xAxis) / (1 / Global.ARCADE_YAW_SENSITIVITY), 1);
		double rightSpeedMod = Math.min(Math.abs(1 / Global.ARCADE_YAW_SENSITIVITY - xAxis) / (1 / Global.ARCADE_YAW_SENSITIVITY), 1);

		robot.leftDriveMotors.set(leftSpeedMod * yAxis * zAxis);
		robot.rightDriveMotors.set(rightSpeedMod * yAxis * zAxis);
	}

}
