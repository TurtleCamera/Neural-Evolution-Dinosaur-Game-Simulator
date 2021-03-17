package DinosaurGame;

// Just a class that stores the information for the dots on the ground
public class Dot {
	public int x; // X coordinate of the dot
	public int y; // Y coordinate of the dot
	public int w; // Width of the dot
	
	final public int groundY = (int) (1080 * (3.0 / 4.0)) - 25;
	
	// Constructor for dots added before the game starts
	public Dot(int x) {
		// Randomly generate the y coordinate of the new dot
		this.x = x;
		y = randomizeY();
		w = randomizeWidth();
	}
	
	// Constructor for new dots added after the game starts
	public Dot() {
		// Randomly generate the y coordinate of the new dot
		x = 1920 + 50; // Start off screen
		y = randomizeY();
		w = randomizeWidth();
	}
	
	public int randomizeY() {
		// Generates a random number between 0-50 below the ground line
		return (int) (Math.random() * 51) +  groundY;
	}
	
	public int randomizeWidth() {
		return (int) (Math.random() * 11) + 3;
	}
}
