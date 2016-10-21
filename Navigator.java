package ev3SearchingForObjects;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigator extends Thread {
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private double curTheta;
	private double curX;
	private double curY;
	private double nextTheta;
	private double rotationAmount;
	private double radius=2.1;
	private double width=15.7;
	private static final double BAND_WIDTH=2; 		//This amount of error is considered tolerable
	private boolean arrived;
	private static final double THETA_TOL=0.1;
	private static final int ROTATE_SPEED=150;
	private static final int FORWARD_SPEED=200;
	private Odometer odometer;
	private int choice;
	private boolean isNavigating;
	
	

	public Navigator(Odometer odometer){ 	//Construct the Navigator object 
		this.leftMotor=odometer.getLeftMotor();
		this.rightMotor=odometer.getRightMotor();
		this.odometer=odometer;
		
		this.arrived=false;
	}
	public void run(){	//Go to the specified points 
		while(true){
			if(choice==1){			//Part 1 of the demo selected 
				travelTo(30, 60);
				travelTo(30, 30);
				travelTo(60, 30);
				travelTo(0, 60);
				arrived=true;	//Tell the robot it has arrived after reaching (60,0)
				try {
					Thread.sleep(2000);			//Wait 2s to read values on the robot
				} catch (InterruptedException e) {
				}
				System.exit(0);		//Exit the program once we have arrived
			}
			if(choice==2){		//Part 2 of the demo selected
				travelTo(60,0);
				travelTo(0,60);
				arrived=true;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}	
				System.exit(0);			
			}
		}
	}
	public void travelTo(double x, double y){
		nextTheta=findNewHeading(x,y);		//Calculate the heading we need to face to get to our destination
		turnTo(nextTheta);					//Turn to that heading
		while(!arrived &&Lab5.getIsNavigating()){	
			
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);			 
			leftMotor.forward();
			rightMotor.forward();
			checkArrived(x,y);		//Keep checking if we've arrived
		}
		if(arrived){				//Stop the motors if we've arrived
			leftMotor.stop();
			rightMotor.stop();
			arrived=false;		//Get ready to go to the next destination by setting arrived to false.
		}
	}
	
	public void checkArrived(double x, double y){		//A method that check if we've arrived within some allowable error.
		curX=odometer.getX();
		curY=odometer.getY();
		double errorX= (curX-x);
		double errorY= (curY-y);
		if(Math.abs(errorX)<BAND_WIDTH && Math.abs(errorY)<BAND_WIDTH){
			arrived=true;
		}
		else{
			arrived=false;
		}
	}
	public void turnTo(double theta){		//Takes a heading, theta, that the robot needs to face and rotates to face that heading.
		if(Math.abs(theta-curTheta) > THETA_TOL){	//Check if the error in our current heading and required heading is acceptable
			curTheta=odometer.getTheta();
			rotationAmount=theta-curTheta; 			//Calculate the amount to rotate to(theta). It is the the heading you want to face minus the current heading
			
			calcMinAmountToRotate(rotationAmount);	//Reduce the amount to a minimum
			//Rotate by that rotation amount
			rotate(rotationAmount);					//Rotate so we are facing our destination
		}
	}		
		
		
	public void rotate(double rotationAmount){	//Rotate by the rotation amount in radians using code from SquareDriver in Lab2
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(radius, width, rotationAmount*180/Math.PI), true);		//Rotate by the rotation amount converted to degrees.
		rightMotor.rotate(-convertAngle(radius, width, rotationAmount*180/Math.PI), false);
		
	}
	
	public double findNewHeading(double nextX, double nextY){
		double theta;
		curX=odometer.getX();
		curY=odometer.getY();
		curTheta=odometer.getTheta();
		theta=Math.atan2(nextY-curY, nextX-curX);
		if( (nextX-curX)<0){					//Get the right result from the binary arctan function even when x is negative
			if(	(nextY-curY) <0){				//When x is negative and y is positive
				theta=theta + Math.PI;
			}
			else{
				theta=theta - Math.PI;			//Both arguments of arctan are negative
				Sound.twoBeeps();
			}
		}
		return theta;
	}		
	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}
	public void calcMinAmountToRotate(double rotationAmount){		//once we know the how much to rotate, reduce this angle to a minimum 
		if(rotationAmount< -Math.PI){
			rotationAmount+= 2*Math.PI;
		}
		else if(rotationAmount>Math.PI){
			rotationAmount-= 2*Math.PI;	
			Sound.twoBeeps();
		}
		this.rotationAmount=rotationAmount;
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	public void setChoice(int choice){		//Set the Navigator's mode for either part 1 or 2 of the demo
		this.choice=choice;
	}
	public int getChoice(){
		return this.choice;
	}
	public void setIsNavigating(boolean state){
		this.isNavigating=state;
	}

}
