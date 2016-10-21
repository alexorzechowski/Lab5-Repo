/*
 * Odometer.java
 */

package ev3SearchingForObjects;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;
		
	//private double distanceTraveled;	
	private double distanceLeft;
	private double distanceRight;
	private double displacement;
	private double deltaX;
	private double deltaY;
	private double deltaTheta;
	private double radius;
	private double wb;
	private double nowTachoRight;
	private double nowTachoLeft;
	private double lastTachoRight;
	private double lastTachoLeft;

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		leftMotor.resetTachoCount();
	    rightMotor.resetTachoCount();
		lock = new Object();
		this.radius=2.1;		//Use the same radius and wheel base and  defined in Lab2
		this.wb=15.7;
		
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		while (true) {
			updateStart = System.currentTimeMillis();
			//TODO put (some of) your odometer code here
			nowTachoRight = rightMotor.getTachoCount();		//Get the current values of each tachoCount
			nowTachoLeft = leftMotor.getTachoCount();
			
			distanceLeft= 3.14159*radius*(nowTachoLeft-lastTachoLeft)/180;		//Find how many centimeters the left and right wheel traveled
			distanceRight= 3.14159*radius *(nowTachoRight-lastTachoRight)/180;		
			lastTachoRight=nowTachoRight;		//Assign the previous tachoCount to be the current one to be used in the next iteration
			lastTachoLeft=nowTachoLeft;
			
																							//Convert new angle to degrees.
			displacement=	0.5*(distanceLeft+distanceRight);								//Calculate how far the robot went 
			deltaTheta =(distanceLeft-distanceRight)/wb;									//Calculate the change in theta in radians
			deltaX = displacement*Math.cos(theta);										//Find how far it went in the x and y directions
			deltaY = displacement*Math.sin(theta);
			
			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */		
				
				theta= theta+deltaTheta;			//Add the change in theta to theta so we can represent how much the robot turned in total
				x=	x+deltaX;						//Add each distance to the total distance for each component
				y= y+deltaY;
				if(theta<0){
					theta=theta+6.28;
				}
				if(theta> 2*Math.PI){						//Ensure theta stays between 0 and 360 degrees
					theta=theta-6.28;			
				}
			
				
				
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}