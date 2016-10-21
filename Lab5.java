package ev3SearchingForObjects;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab5 {
	// Left motor connected to output A
	// Right motor connected to output D
		private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		private static final Port usPort = LocalEV3.get().getPort("S1");		
		private static final Port colorPort = LocalEV3.get().getPort("S2");	
		private static Object lock;
		
		private static boolean isNavigating=true; 		//Assume that there are no obstacles when the robot starts up.
		
		public static void setIsNavigating(boolean state){ 		//Ensure the 2 threads can safely read and write isNavigating.
			synchronized (lock){
				isNavigating=state;
				}
		}
		public static boolean getIsNavigating(){
			synchronized (lock){
				return isNavigating;
			}
		}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//Set up the US Sensor
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Set up the color sensor
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("ColorID");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
		
		int buttonChoice;
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		Navigator navigator = new Navigator(odometer);
		
		//ObstacleAvoidance obstacleAvoidance = new ObstacleAvoidance(leftMotor, rightMotor, odometer, navigator);
		lock=new Object();
		do {
			// Use display structure from Lab2 to choose mode 1 or 2
			t.clear();

			// ask the user what part of the demo to do
			t.drawString("< Left | Right > ", 0, 0);
			t.drawString("       |         ", 0, 1);
			t.drawString("Part 1 | Part 2  ", 0, 2);
			t.drawString("	     |         ", 0, 3);
			t.drawString("       |         ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);
		
		
		if (buttonChoice == Button.ID_LEFT) {
			odometer.start();		//If the user selects Demo Part 1, don't start the obstacleAvoidance Thread
			navigator.start();
			// perform the ultrasonic localization
			ObjectFinder objectFinder = new ObjectFinder(odometer, usValue, usData, navigator);
			ObjectID identifier = new ObjectID(odometer, navigator, colorValue, colorData);
			Printer printer = new Printer(identifier, objectFinder);
			identifier.start();
			objectFinder.start();
			printer.start();
		
		} else {					//If the user selects Demo Part 2, start the obstacleAvoidance Thread and all other Threads
				
		odometer.start();
		navigator.start();
		//obstacleAvoidance.start();	
		USLocalizer usl = new USLocalizer(odometer, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE, navigator);
		usl.doLocalization();
		}
		Button.waitForAnyPress();
		System.exit(0);
		
	}

}
