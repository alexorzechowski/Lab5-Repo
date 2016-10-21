package ev3SearchingForObjects;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObjectID extends Thread {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigator navigator;
	private int color;
	private boolean isBLock;
	
	public ObjectID(Odometer odo, Navigator navigator, SampleProvider colorSensor, float[] colorData){
		this.odo = odo;
		this.navigator=navigator;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.rightMotor=odo.getRightMotor();
		this.leftMotor=odo.getLeftMotor();
	}
	
	public void LocateObjects(){
		while(true){
			getColorData();			
		}
		
	}
	public int getColorData(){
		colorSensor.fetchSample(colorData, 0);
		color = (int) colorData[0];
		try{
			Thread.sleep(50);
		}
		catch(Exception e){ }
		return color;
	}
	public String getColorName(){
		String colorName;
		if(color==2){
			colorName="Blue";
		}
		else if(color==7){
			colorName="Brown";
		}
		else{
			colorName="Not Blue or Brown";
		}
		return colorName;
	}
	public boolean getIsBlock(){
		if(getColorData()==7){		//Check if the color is brown to see if the object is a block
			return true;
		}
		else{
			return false;
		}
	}
}
