package DinosaurGame;

import java.util.ArrayList;

public class Ground {
	final public int groundY = (int) (1080 * (3.0 / 4.0)) - 25; // The y coordinate of the ground's line
	ArrayList<Dot> dots;
	
	// Constructor
	public Ground() {
		// Generate 15 dots to start off
		dots = new ArrayList<Dot> ();
		
		int tempX = 60; // Location of the first dot
		for(int i = 0; i < 16; i ++) {
			dots.add(new Dot(tempX));
			tempX += 120; // Estimated distance between each dot
		}
	}
	
	public void updateLocations(int speed) {
		// Update the location for each dot in the array list
		for(int i = 0; i < dots.size(); i ++) {
			// Shift the dot left
			dots.get(i).x -= speed;
			
			// If the rightmost side of the dot is off screen, just remove it
			if(dots.get(i).x + dots.get(i).w < 0) {
				dots.remove(i);
				i --;
			}
		}
		
		// While we are here, see if we can add a new dot
		// If there are 70 pixels between the left most side of the dot and the end of the right side of the screen, then add a new dot
		if(1920 - dots.get(dots.size() - 1).x >= 70) {	// 70 because we start the dot 50 pixels off screen
			dots.add(new Dot());
		}
	}
}
