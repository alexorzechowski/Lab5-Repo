package ev3SearchingForObjects;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Printer extends Thread {			//Incorporate printer from Lab1
	
	private ObjectID ojectIdentifier;
	private ObjectFinder objectFinder;
	public static TextLCD t = LocalEV3.get().getTextLCD();	
	
	public Printer(ObjectID objectIdentifier, ObjectFinder objectFinder){
		this.ojectIdentifier=objectIdentifier;
		this.objectFinder=objectFinder;
	}
	
	public void run() {
		while (true) {														// operates continuously
			t.clear();
			if(objectFinder.getObjectFound()){
				t.drawString("Object Detected!", 0, 1);
				
				if(ojectIdentifier.getIsBlock()==true){
					t.drawString("Block!", 0, 2);
				}
				else{
					t.drawString("Not Block!", 0, 3);
				
				}
			}
			try {
				Thread.sleep(200);											// sleep for 200 mS
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
}
