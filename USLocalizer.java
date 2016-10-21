package ev3SearchingForObjects;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static final int ROTATION_SPEED = 100;

	private Odometer odo;
	private SampleProvider usSensor;
	private Navigator navigator;
	private float[] usData;
	private LocalizationType locType;
	private static final int EDGE_DETECTED_VAL=40;	//Define a value under which you face a wall.
	private static final int FILTER_OUT=4;
	private int filterControl=0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private double radius=2.2;
	private double width = 15.8;

	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigator navigator) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navigator=navigator;
		this.leftMotor=odo.getLeftMotor();
		this.rightMotor=odo.getRightMotor();
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall - FALLING EDGE ROUTINE - robot starts facing away from wall
			while(getFilteredData()<=EDGE_DETECTED_VAL){	
				
			navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);	// Force initial condition. 
																	//ie Make robot see no wall - turn clockwise
				
			}		
			
			// keep rotating until the robot sees a wall, then latch the angle 
			//ie Get a falling edge
			while(getFilteredData()> EDGE_DETECTED_VAL){
				navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);		//Keep turning clock-wise until you see a wall
			}
			angleA=odo.getTheta();	//Get the first angle		
			Sound.beep();
			// switch direction and wait until it sees no wall
			navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);	//Turn other way a little bit then, start looking for walls again
			applyDelay();
			//ie get another falling edge			
			while(getFilteredData()>EDGE_DETECTED_VAL){	
				navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);		//Rotate counter-clockwise, Turn until robot sees no wall
			}
			angleB=odo.getTheta();	//Latch angle when robot sees a wall
			Sound.twoBeeps();
			if(angleA > angleB){	//Enforce angle A to be less than angle B
				angleA = angleA - 360;
			}
			// keep rotating until the robot sees a wall, then latch the angle
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			// update the odometer position (example to follow:)
			double zeroDeg= angleB - (angleA+angleB)/2 -36;		//Define 0 degrees to be along the positive y axis
			leftMotor.rotate(convertAngle(radius, width, zeroDeg), true);
			rightMotor.rotate(-convertAngle(radius, width, zeroDeg), false);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true}); //Call this heading 0 degrees.
			
			Sound.playTone(100, 100);
			Button.waitForAnyPress();	//Wait to go on to demo part 2 
			} else {
			/* RISING EDGE ROUTINE - robot starts facing a wall
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			while(getFilteredData()>EDGE_DETECTED_VAL){					//Force the initial condition
				navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);	//Make sure robot starts facing a wall, rotate clockwise
			}
			//Keep rotating clockwise until we see no wall (Get a rising edge)
			while(getFilteredData()<EDGE_DETECTED_VAL){
				navigator.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			angleA=odo.getTheta();
			//Keep rotating counter-clockwise until we get another rising-edge
			navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);	//Get robot ready to detect another rising edge by rotating a little into the wall
			applyDelay();
			while(getFilteredData()<EDGE_DETECTED_VAL){
				navigator.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			angleB=odo.getTheta();
			double zeroDeg= angleB - (angleA+angleB)/2 -220;
			leftMotor.rotate(convertAngle(radius, width, zeroDeg), true);
			rightMotor.rotate(-convertAngle(radius, width, zeroDeg), false);
			//Point to heading 0deg
			odo.setPosition(new double[] {0.0,  0.0, 0.0}, new boolean[]{false, false, true}); //Set odometer to 0 degrees
			//Button.waitForAnyPress();	//Wait to go on to demo part 2 
		}
	
	}
	//Methods provided to us in previous labs
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	public void applyDelay(){
		try{
			Thread.sleep(3000);
		}
		catch(Exception e){
		}
	}
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = (usData[0]*100);
				if(distance>50 && filterControl < FILTER_OUT){
					filterControl ++;
				}
				else if (distance > 50){
					// We picked up many large values in a row, so assume there is nothing in front of the sensor
					//Leave distance 
					distance=50;
				}
				else{
					filterControl=0;
				}
		return distance;
	}

}
