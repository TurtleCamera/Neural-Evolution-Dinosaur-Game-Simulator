package DinosaurGame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Game {
	public static GameFrame frame; // The frame that will display the game
	public static PaintPanel paint; // The panel that draws the objects
	public static JButton machine; // Button for machine learning option
	public static JButton human; // Button for manual/normal game play
	
	public static String state; // Determines if we are in the menus or one of the game modes
	public static boolean inGame; // Determines if we are in game

	// Objects for the game
	public static Ground ground;
	public static ArrayList<Cactus> obstacles; // All obstacles that are on screen
	public static Clip hundredSound; // Sound file for playing 
    	public static File soundFile = new File("hundred.wav"); // File for playing the hundred score reached sound
	public static Population pop;
	
	// Variables for the game
	public static double speed; // Speed of incoming obstacles
	public static int speedMultiplier; // Controlled by the player to speed or slow down the game
	public static double cactusTimer; // Counter for the next cactus
	public static double nextCactus; // Ticks until the next cactus
	public static double currentScore; // Score of the current game
	public static double scoreThreshold; // Used to determine whether or not we hit 100
	public static int bestAlivePlayerIndex; // Stores the index of the best alive player (used for the paint panel for drawing the best alive player's network)
	public static int alivePlayerCount; // Counts the amount of dinosaurs alive
	public static String selectedDinosaurOption; // This is the amount of dinosaurs that the user selected
	public static OptionBox options;
	
	// Variables that help prevent multiple triggers of some methods
	public static boolean firstPopulation; // Changes to false only once (used to tell if we initialized the game for the first time
	
	public static void main(String[] args) throws InterruptedException, IOException, UnsupportedAudioFileException, LineUnavailableException {
		createFrame();
		createPanel();
		createButtons();
		state = "menu";
		inGame = false;
		speed = 10;
		currentScore = Population.bestScore = 0;
		speedMultiplier = 1;
		firstPopulation = true;
		scoreThreshold = 100;
		alivePlayerCount = 0;
		selectedDinosaurOption = "unselected";
		
		// The loop for updating the game begins
		while(true) {
			Thread.sleep(17); // Close to 60 FPS
			
			// Remove all components only once to set up for the game
			if(!state.equals("menu")) {
				if(!inGame) {
					removeComponents();
					
					if(state.equals("AI")) {
						// Create a drop down menu for the user
						// Prompt the user for amount of dinosaurs per generation
						options = new OptionBox();
						while(selectedDinosaurOption.equals("unselected")) {
							// Do nothing until the user selects something
							// Sleeping thread because this while loop freezes the program
							Thread.sleep(100);
						}
					}
				    
				    // Create the objects and start the game
					createObjects();
					
					inGame = true;
				}
			}

			// Code to handle either mode of the game
			if (state.equals("AI")) {
				// Update all objects first because it has the code that makes dinosaurs "think"
				updateAll();
				
				// Call the method that runs the game with a human
				aiGame();
			}
			else if(state.equals("human")) {
				// Call the method that runs the game with a human
				humanGame();

				// Update all objects
				updateAll();
			}
			
			// Redraw the game
			paint.repaint();
		}
	}

	public static void aiGame() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		for(int i = 0; i < pop.population.size(); i ++) {
			// Only perform this check if the dinosaur is alive
			if(!pop.population.get(i).isDead) {
				for(int j = 0; j < obstacles.size(); j ++) {
					if(obstacles.get(j).collisionDetection(pop.population.get(i).x, pop.population.get(i).y, pop.population.get(i).w, pop.population.get(i).h)) {
						// Player died, so kill him off
						pop.population.get(i).playDeath();
						pop.population.get(i).isDead = true;
					}
				}
			}
		}
		
		// Set the best alive player's index for the paint panel to draw out
		setBestAlivePlayerIndex();
		
		// Check if all dinosaurs are dead
		if(pop.isAllDead()) {
			// The AI game restarts much differently because we are now evolving it
			currentScore = 0;
			speed = 10;
			scoreThreshold = 100;
			
			// Create a new generation
			createObjects();
			
			// Clear memory
			System.gc();
		}
	}
	
	public static void humanGame() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		for(int i = 0; i < obstacles.size(); i ++) {
			if(obstacles.get(i).collisionDetection(pop.population.get(0).x, pop.population.get(0).y, pop.population.get(0).w, pop.population.get(0).h)) {
				// The only player died, so restart the game immediately
				pop.population.get(0).playDeath();
				createObjects();
				currentScore = 0;
				speed = 10;
				scoreThreshold = 100;
			}
		}
	}
	
	public static void playHundred() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Reload sounds
		loadSounds();
		
		// Play jump sound
		hundredSound.start();
	}
	
	public static void loadSounds() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Load the sound for jump
		// Open an audio input stream.           
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);    
        
        // Get a sound clip resource.
        hundredSound = AudioSystem.getClip();
        
        // Open audio clip and load samples from the audio input stream.
        hundredSound.open(audioIn);
	}

	
	public static void updateAll() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		// Update the ground
		ground.updateLocations((int) speed * speedMultiplier);

		// Update the tick based variables for cacti
		if(cactusTimer >= nextCactus) {
			nextCactus = 45 + Math.random() * 36; // Ticks before next cactus between 45-80 ticks
			cactusTimer = 0;
			obstacles.add(new Cactus());
		}
		else {
			cactusTimer += speedMultiplier;
		}
		
		// Update the cacti themselves
		for(int i = 0; i < obstacles.size(); i ++) {
			// If after updating the location of the cactus the cactus is off screen, then remove it
			if(obstacles.get(i).updateLocation((int) speed * speedMultiplier)) {
				obstacles.remove(i);
				i --;
			}
		}
		
		// Update the dinosaurs
		pop.updatePopulation();
		
		// Update score and speed
		currentScore += speed * speedMultiplier / 76;
		if(Population.bestScore < (int) currentScore) {
			Population.bestScore = (int) currentScore;
		}
		
		if(currentScore >= scoreThreshold && (int) (currentScore) != 0) {
			scoreThreshold += 100;
			speed += 1;

			// Play sound for reaching +100
			playHundred();
		}
	}
	
	public static void createObjects() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		ground = new Ground();
	
		obstacles = new ArrayList<Cactus> ();
		obstacles.add(new Cactus());
		cactusTimer = 0;
		nextCactus = 45;
		
		if(state.equals("human")) {
			// Normal mode only has 1 player controlled dinosaur
			pop = new Population(1);
		}
		else if(state.equals("AI")) {
			// Self-learning AI mode 100 AI controlled dinosaur
			// Only create the starting population if the game was initialized for the first time
			if(firstPopulation) {
				// Don't crash when selecting 50 because the options have it set to string "50 (Default)"
				if(selectedDinosaurOption.equals("50 (Default)")) {
					selectedDinosaurOption = "50";
				}
				else if(selectedDinosaurOption.equals("100 (May have slight lag)")) {
					selectedDinosaurOption = "100";
				}
				else if(selectedDinosaurOption.equals("150 (May have slight lag)")) {
					selectedDinosaurOption = "150";
				}
				else if(selectedDinosaurOption.equals("200 (May have considerable lag)")) {
					selectedDinosaurOption = "200";
				}
				else if(selectedDinosaurOption.equals("250 (May have considerable lag)")) {
					selectedDinosaurOption = "250";
				}
				else if(selectedDinosaurOption.equals("500 (Strong computer required)")) {
					selectedDinosaurOption = "500";
				}
				else if(selectedDinosaurOption.equals("750 (Not recommended)")) {
					selectedDinosaurOption = "750";
				}
				else if(selectedDinosaurOption.equals("1000 (Not recommended)")) {
					selectedDinosaurOption = "1000";
				}
				
				pop = new Population(Integer.parseInt(selectedDinosaurOption));
				firstPopulation = false;
			}
			else {
				pop.naturalSelection();
			}
		}
		
		// Load the sound for reaching a score of 100
		// Open an audio input stream.           
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);    
        
        // Get a sound clip resource.
        hundredSound = AudioSystem.getClip();
        
        // Open audio clip and load samples from the audio input stream.
        hundredSound.open(audioIn);
	}
	
	public static void removeComponents() {
		// Remove components (only called once when we are in game)
		paint.removeAll();
		frame.repaint();
	}
	
	public static void createPanel() {
		// Create and add the paint panel to the GameFrame
		paint = new PaintPanel();
		paint.setPreferredSize(new Dimension(1920, 1080));
		frame.add(paint);
	     
	    // Display welcoming text
	    JLabel text = new JLabel("Dinosaur Self-Learning AI Game", SwingConstants.CENTER);
	    text.setPreferredSize(new Dimension(1200, 400));
	    text.setFont(text.getFont().deriveFont(52.0f));
	    paint.add(text, BorderLayout.NORTH);
	}
	
	public static void createFrame() {
		//Create the window
	    frame = new GameFrame();
	    frame.setDefaultCloseOperation(GameFrame.EXIT_ON_CLOSE);
	    frame.setSize(1920, 1080);
	    
	    //Display the window.
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);     
	    frame.setFocusable(true);
	    frame.addKeyListener(frame);
	}
	
	public static void createButtons() {
		// New panel to add the buttons to
		JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new GridLayout(12,12));
	    
	    // Instantiate the buttons and add listeners to them
		machine = new JButton("AI Self-Learning Mode");
		human = new JButton("Normal Mode");
		machine.addActionListener((ActionListener) new ButtonListener());
		human.addActionListener((ActionListener) new ButtonListener());
		
		// Add more glues to fill blanks spots in the beginning of the panel
		for(int i = 0; i < 12 * 12; i ++) {
			if(i == 135) {
				// Row 12 column 3
				buttonPanel.add(machine);
			}
			else if(i == 140) {
				// Row 12 column 8
				buttonPanel.add(human);
			}
			else {
				// Every other spot on the grid
				buttonPanel.add(new WhitePanel());
			}
		}
		
		// Add this panel to the bottom of the screen
		paint.add(buttonPanel, BorderLayout.SOUTH);
		paint.validate();
	}
	
	public static void setBestAlivePlayerIndex() {
		// Stops searching for the best alive player if we already found it
		boolean found = false; 
		
		// While we are here, lets count the amout of players alive
		alivePlayerCount = 0;
		for(int i = 0; i < pop.population.size(); i ++) {
			if(!pop.population.get(i).isDead) {
				alivePlayerCount ++;
				
				if(!found) {
					bestAlivePlayerIndex = i;
					found = true;
				}
			}
		}
		// If we reach the end, that means all of the dinosaurs died, but it's nothing to worry about
	}
}
