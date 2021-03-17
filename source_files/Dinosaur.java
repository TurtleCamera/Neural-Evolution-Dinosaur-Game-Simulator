package DinosaurGame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Dinosaur {
	public final int x; // Leftmost x coordinate of the player
	public int y; // Bottom y coordinate of the player
	public int h; // Height of the player
	public int w; // Width of the player

	public double velocity; // Velocity of the y coordinate of the player
	public double gravity; // Gravity/acceleration of the velocity

	public int animationCount; // Used to determine when to change the walking animation of the dinosaur
	public static BufferedImage image1; // Image of the dinosaur (left foot down)
	public static BufferedImage image2; // Image of the dinosaur (right foot down)
	public static BufferedImage currentImage; // Current image of the dinosaur
	
	public static Clip jumpSound; // Sound used for jumping
	public static Clip deathSound; // Sound used for dying
	public static File jumpsoundFile= new File("jump.wav"); // File for jump sound loaded into memory
	public static File deathsoundFile= new File("death.wav"); // File for death sound loaded into memory
	
	public boolean bigJump; // Used for determining if the jump was a big or small jump
	public boolean duplicateScalar; // Prevents the scalers for speedMultiplier from happening more than once
	public int previousScalar;
	
	// Variables for the dinosaur's neural network
	public int fitness; // Fitness based off the score (in fact, it's equal to score * score)
	public Network n; // The network that represents the "brain" of this dinosaur
	public int inputNodes; // Amount of input Nodes
	public int outputNodes; // Amount of output Nodes
	public boolean isDead; // Tells if the dinosaur is dead
	public int score; // The score during a game
	public int bestScore; // This dinosaur's best score (not actually used, but I have it here just in case I need it later)
	public int generation; // The generation this dinosaur is in
	final public int inputs = 6; // Fixed number of inputs
	final public int outputs = 2; // Fixed number of outputs
		
	float[] dataCollection = new float[inputs]; // Inputs from the input nodes (array used to feed into the neural network [the vision of this dinosaur])
	float[] outputDecision = new float[outputs]; // The output values from the dataCollection input array

	public Dinosaur() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		x = 1920 / 8; // Player isn't actually moving relative to the screen, so we fix its location
		y = (int) (1080 * (3.0 / 4.0)) + 8; // Start the player on the ground
		w = 96;
		h = 112;

		velocity = 0;
		gravity = 0;

		animationCount = 5 - (int) (Math.random() * 10); // Start somewhere between -4 and 5 for the animation ticks
		image1 = ImageIO.read(new File("dinoLeft.png"));
		image2 = ImageIO.read(new File("dinoRight.png"));
		currentImage = image1; // This will get updated when the game actually starts
		
		loadSounds();
		
		bigJump = true;
		duplicateScalar = false;
		previousScalar = Game.speedMultiplier;
		
		fitness = 0;
		inputNodes = 6;
		outputNodes = 2;
		score = 0;
		bestScore = 0;
		generation = 0;
		isDead = false;
		n = new Network(inputNodes, outputNodes);
	}

	public void loadSounds() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Load the sound for jump
		// Open an audio input stream.          
		AudioInputStream audioIn;
        
		// Get the jump found
		audioIn = AudioSystem.getAudioInputStream(jumpsoundFile);    
	        
		// Get a sound clip resource.
		jumpSound = AudioSystem.getClip();
	        
		// Open audio clip and load samples from the audio input stream.
		jumpSound.open(audioIn);

		// Load the sound for death
		audioIn = AudioSystem.getAudioInputStream(deathsoundFile);    
	        
		// Get a sound clip resource.
		deathSound = AudioSystem.getClip();
	        
		// Open audio clip and load samples from the audio input stream.
		deathSound.open(audioIn);
	}
	
	public void bigJump() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Don't jump if airborne
		if(!airborne()) {
			// Don't do anything if the speedMultiplier is 0 (since no one is moving)
			if(Game.speedMultiplier > 0) {
				// If the player is on the ground, perform a big jump
				if (y >= 1080 * (3.0 / 4.0) + 8) {
					gravity = 1.3;	// 5 -100
					velocity = -25.4; // Negative because JFrames flip the y-axis
					
					// Play jump sound
					jumpSound.start();
					
					// Reload sounds 
					loadSounds();
				}
			}
		}
	}

	public void smallJump() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Don't jump if airborne
		if(!airborne()) {
			// Don't do anything if the speedMultiplier is 0 (since no one is moving)
			if(Game.speedMultiplier > 0) {
				// Please note that the manual game mode doesn't support small jumps
				// If the player is on the ground, perform a small jump
				if (y >= 1080 * (3 / 4)) {
					gravity = 1.56;
					velocity = -22.72;
					
					// Play jump sound
					jumpSound.start();
					
					// Reload sounds
					loadSounds();
				}
			}
		}
	}

	public void applyAirborneScalars() {
		// Because of the speedMultiplier variable in the Game class, we need to update velocity and acceleration if the dinosaur is airborne
		if(airborne()) {
			// Scale the velocity and acceleration (based off kinematic equations), but only if we haven't done so already or if the speed modifier changed
			// Equations used:
			// vf = vi + a * t
			// vi = -vf
			// d = v * t
			// Derived equations:
			// v = (a * t) / 2
			// t = d / v = d / ((a * t) / 2) --> t^2 = (2 * d) / a
			if(!duplicateScalar || previousScalar != Game.speedMultiplier) {
				// If we triggered this part of code because of a change in the speedMultiplier while airborne (not due to duplicateScalar being false), then scale down the previous speed Modifier
				if(previousScalar != Game.speedMultiplier && duplicateScalar) {
					// Avoid dividing by 0
					if(previousScalar == 0) {
						previousScalar = 1;
					}
					
					gravity = gravity / previousScalar;
					velocity = velocity / previousScalar;
				}
					
				// Apply scalars
				if(bigJump) {
					gravity = 1.3 * Math.pow(Game.speedMultiplier, 2);
					velocity = velocity * Game.speedMultiplier;	
				}
				else {
					gravity = 1.56 * Math.pow(Game.speedMultiplier, 2);
					velocity = velocity * Game.speedMultiplier;
				}
				
				duplicateScalar = true;
				previousScalar = Game.speedMultiplier;
			}
		}
	}
	
	public void updateLocation() {
		if(!isDead) {
			// Change velocity and acceleration in the air if the speedMultiplier changes
			applyAirborneScalars();
			
			// Since the player technically isn't moving on the x-axis, only update the y coordinate
			y += velocity; // Move the player up if jumping (if velocity isn't 0)
			velocity += gravity; // Decelerate the velocity
	
			// If the dinosaur was falling, move the dinosaur back to surface level if it's
			// y coordinate ended up below the ground
			if (y >= 1080 * (3.0 / 4.0) + 8) {
				y = (int) (1080 * (3.0 / 4.0)) + 8;
				gravity = 0;
				velocity = 0;
			}
	
			// Only update the image of the dinosaur if it isn't airborne
			if (!airborne()) {
				// While we are here, lets update the animation of the dinosaur
				animationCount += Game.speedMultiplier;
				if (animationCount > 5) {
					animationCount = -4;
				}
	
				if (animationCount <= 0) {
					currentImage = image1;
				} else {
					currentImage = image2;
				}
				
				// Also set duplicateScalar to false
				duplicateScalar = false;
			}
			
			// While we are here, lets also store the score of the game
			// To prevent the score from updating after the game resets, don't store a the game score if it's under score
			if(Game.currentScore >= score) {
				score = (int) Game.currentScore;
			}
		}
	}

	public boolean airborne() {
		if (y < 1080 * (3.0 / 4.0) + 8) {
			return true;
		}

		return false;
	}
	
	public void playDeath() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Play jump sound
		deathSound.start();
		
		// Reload sounds
		loadSounds();
	}  
	
	public Dinosaur breedParents(Dinosaur parent) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		// Create an offspring by calling the crossover method for the Network class on this dinosaur and the parameter dinosaur
		Dinosaur offspring = new Dinosaur();
		offspring.n = n.breedNetwork(parent.n);
		
		// Link the nodes and return the new offspring
		offspring.n.linkNetwork();
		return offspring;
	}
	
	public void calculateFitness() {
		// As mentioned above, the fitness is just score * score
		fitness = score * score;
	}
	
	public int findClosestCactus() {
		// Return -1 if there aren't even any cacti
		if(Game.obstacles.size() <= 0) {
			return -1;
		}
		
		int shortestIndex = Game.obstacles.size() - 1;
		for(int i = Game.obstacles.size() - 1; i >= 0; i --) {
			// Check to make sure that the hitbox of the cactus is in front of the hitbox of the dinosaur
			if(Game.obstacles.get(i).x > x + w) {
				// Now check if it's the closest to the dinosaur (we are using the distance between the left side of the dinosaur and the right side of the cactus as the distance)
				if(Game.obstacles.get(i).x + Game.obstacles.get(i).w - x < Game.obstacles.get(shortestIndex).x + Game.obstacles.get(shortestIndex).w - x) {
					shortestIndex = i;
				}
			}
		}
		
		// If the shortestIndex is not found or happens to be an object behind the dinosaur (only happens if it's the only one on the screen), return -1
		if(Game.obstacles.get(shortestIndex).x <= x + w) {
			return -1;
		}
		else {
			// Return the shortest distance to a cactus in front of the dinosaur
			return shortestIndex;
		}
	}
	
	public int getGapBetweenNextCactus(int closestCactusIndex) {
		// Check to make sure there is another cactus ahead of the closest cactus in the first place
		if(Game.obstacles.size() - closestCactusIndex - 1 > 0) {
			return Game.obstacles.get(closestCactusIndex + 1).x - Game.obstacles.get(closestCactusIndex).x;
		}
		else {
			// Cactus doesn't exist, so it's just 0
			return 0;
		}
	}
	
	public void recordInputs() {
		// Get the index of the cactus that's closest to the dinosaur
		// Node that some of these values are inverted because we want to incentivize jumping if an object is closer
		int closestCactus = findClosestCactus();
		
		// Make sure there was a cactus found
		if(closestCactus < 0) {
			// See the else statment for notes on these array slots
			dataCollection[0] = 0;
			dataCollection[1] = 0;
			dataCollection[2] = 0;
			dataCollection[3] = 0;
			dataCollection[4] = (int) (1080 * (3.0 / 4.0)) + 8 - y;
			dataCollection[5] = 0;
		}
		else {
			dataCollection[0] = 100.0f / (Game.obstacles.get(closestCactus).x + Game.obstacles.get(closestCactus).w - x); // Node 1: Distance between dinosaur and closest incoming cactus
			dataCollection[1] = Game.obstacles.get(closestCactus).h; // Node 2: Height of the closest incoming cactus
			dataCollection[2] = Game.obstacles.get(closestCactus).w; // Node 3: Width of the closest incoming cactus
			dataCollection[3] = (float) (Game.speed * Game.speedMultiplier); // Node 4: Speed of the dinosaur (takes into account the speed multiplier too)
			dataCollection[4] = (int) (1080 * (3.0 / 4.0)) + 8 - y; // Node 5: Current y position of the dinosaur (More like distance off ground since the y-axis is inverted)
			// Avoid divide by 0 error
			if(getGapBetweenNextCactus(closestCactus) == 0)
			{
				dataCollection[5] = 0;
			}
			else {
				dataCollection[5] = 400.0f / getGapBetweenNextCactus(closestCactus); // Node 6: Gap between closest cactus and the next one
			}
		}
	}
	
	public void makeDecision() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		int maxOutputIndex = 0;
		
		// Feed forward in the network to get the outputs
		outputDecision = n.feedInputs(dataCollection);
		
		// Get the index with the biggest output value
		for (int i = 0; i < outputDecision.length; i++) {
			if (outputDecision[i] > outputDecision[maxOutputIndex]) {
		        maxOutputIndex = i;
		    }
		}
		
		// Threshold for output is 0.7 in case they are trigger happy (basically means do nothing)
		if(outputDecision[maxOutputIndex] < 0.7) {
			return;
		}
		
		switch(maxOutputIndex) {
			case 0:
		      smallJump();
		      break;
		    case 1:
		      bigJump();
		      break;
		}
	}
	
	public Dinosaur clone() {
		// Performs an almost deep copy of this dinosaur
		Dinosaur d = null;
		
		// Because you can't throw exceptions in clone, you have to surround with try and catch, but we also don't want to return a null object if something crashes
		while(d == null) {
			try {
				d = new Dinosaur();
				d.n = n.clone();
				d.fitness = fitness;
				d.n.linkNetwork(); 
				d.generation = generation;
				d.bestScore = score;
				d.score = score;
			} catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		
		return d;
	}
}
