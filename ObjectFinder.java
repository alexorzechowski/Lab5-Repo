package ev3SearchingForObjects;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObjectFinder extends Thread{
	//Use US sensor already created

	private Odometer odometer;
	private SampleProvider usSensor;
	private Navigator navigator;
	private float[] usData;
	private int filterControl=0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private static final int FILTER_OUT=4;
	private static final int OBJECT_FOUND_DISTANCE=8;	//Define distance under which we will say that some object is found.
	private boolean objectFound;
	
	public ObjectFinder(Odometer odometer, SampleProvider usSensor, float[] usData, Navigator navigator){
		this.odometer = odometer;
		this.usSensor = usSensor;
		this.usData = usData;
		this.navigator=navigator;
		this.leftMotor=odometer.getLeftMotor();
		this.rightMotor=odometer.getRightMotor();
	}
	public void run(){				//Look for anything that might appear in front of the robot
		while(true){
			int distance = (int)getFilteredData();
			if(distance<OBJECT_FOUND_DISTANCE){
				objectFound=true;
			}
			else{
				objectFound=false;
			}
		}
	}
	public boolean getObjectFound(){	//access to the objectFound variable 
		return objectFound;
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
