package DinosaurGame;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Population {
	public ArrayList<Dinosaur> population; // The population of the game
	Dinosaur bestPlayer; // Best player in all generations (not used, but I have it here just in case I need it later) (I do use the best player in the species class though)
	public static int bestScore; // Best score in all generations
	
	public ArrayList<Species> species; // Population is divided into species
	ArrayList<History> history; // Stores each mutation (node, connection, weight) created
	
	public int generation; // The generation we are on
	
	// Constructor
	public Population(int size) throws IOException, UnsupportedAudioFileException, LineUnavailableException {	
		species = new ArrayList<Species> ();
		history = new ArrayList<History> ();
		
		population = new ArrayList<Dinosaur> ();
		for(int i = 0; i < size; i ++) {
			population.add(new Dinosaur());
			population.get(i).n.linkNetwork();
			population.get(i).n.mutate(history);
		}
		
		// Don't initialize best player yet, but initialize best score
		bestScore = 0;
		
		generation = 1;
	}
	
	public void updatePopulation() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Update all dinosaurs
	    for (int i = 0; i < population.size(); i++) {
	    	// Only update those who are alive
	    	if (!population.get(i).isDead) {
	    		// Only do this if we are running the AI
	    		if(Game.state.equals("AI")) {
		    		// Gather data from the game to use in the neural network
		    		population.get(i).recordInputs(); 
		    		// Feed the inputs forward and make a decision
		    		population.get(i).makeDecision();
	    		}

	    		// Both AI and human mode run this
		        population.get(i).updateLocation();
	        }
	    }
	}
	
	public boolean isAllDead() {
		// Check to see if all dinosaurs are dead
		for(int i = 0; i < population.size(); i ++) {
			// If one dinosaur is alive, then we are not done
			if(!population.get(i).isDead) {
				return false;
			}
		}
		
		// All dinosaurs are dead
		return true;
	}
	
	public Dinosaur findBestDinosaur() {
		int highestScoreIndex = 0;
		for(int i = 0; i < population.size(); i ++) {
			if(population.get(i).score >= population.get(highestScoreIndex).score) {
				highestScoreIndex = i;
			}
		}
		
		return population.get(highestScoreIndex);
	}
	
	public void setBestPlayer() {
		// Set the best player if it did better than the best score
		Dinosaur bestInGeneration = findBestDinosaur();
				
		if(bestInGeneration.score >= bestScore) {
			bestScore = bestInGeneration.score;
			bestPlayer = bestInGeneration.clone();
		}
	}
	
	public void seperateIntoSpecies() {
		// Clear the players in the species of this generation
		for(Species s: species) {
			s.dinosaurs.clear();
		}
		
		// Now we seperate the players into species
		for(int i = 0; i < population.size(); i ++) {
			// We need to see if each player has a similar enough network to one of the species 
			boolean similar = false;
			for(Species s: species) {
				// Perform the check I mentioned above
				if(s.similarSpecies(population.get(i).n)) {
					s.add(population.get(i));
					similar = true;
					break;
				}
			}
			
			// If this player can't be categorized into any of the other species, then make a new species
			if(!similar) {
				species.add(new Species(population.get(i)));
			}
		}
		
		// Populate species that don't have any players in them
		for(int i = 0; i < species.size(); i ++) {
			if(species.get(i).dinosaurs.size() <= 0) {
				species.get(i).dinosaurs.add(species.get(i).bestPlayer.clone());
			}
		}
	}
	
	public void calculateAllFitness() {
		// Basically calls the fitness calculation method on all players
		for(int i = 0; i < population.size(); i ++) {
			population.get(i).calculateFitness();
		}
		
		// Calculate for any species that were populated
		for(int i = 0; i < species.size(); i ++) {
			for(int j = 0; j < species.get(i).dinosaurs.size(); j ++) {
				species.get(i).dinosaurs.get(j).calculateFitness();
			}
		}
	}
	
	public void selectionSort() {
		// Does what you think it does
		for(int i = 0; i < species.size(); i ++) {
			int highestFitnessPosition = i;
			
			for(int j = i; j < species.size(); j ++) {
				if(species.get(j).dinosaurs.get(0).fitness > species.get(highestFitnessPosition).dinosaurs.get(0).fitness) {
					highestFitnessPosition = j;
				}
			}
			
			Species temp = species.get(i);
			species.set(i, species.get(highestFitnessPosition));
			species.set(highestFitnessPosition, temp);
		}
	}
	
	public void sortSpecies() {
		// Before we do anything, lets call the sort method on all of the species
		for(Species s: species) {
			s.sortDinosaurs();
		}
		
		// Now we just sort the species based off the fitness value of each species' best player
		selectionSort();
	}
	
	public void removeStaleSpecies() {
		// Kill species who have too high of a staleness value
		// We should skip the best species to give them a chance, but I also don't want an out of bounds exception
		int startingIndex;
		if(species.size() >= 2) {
			startingIndex = 1;
		}
		else {
			startingIndex = 0;
		}
		
		for(int i = startingIndex; i < species.size(); i ++) {
			// Set threshold to 5 generations
			if(species.get(i).stale >= 5) {
				// Don't remove the best species though
				if(i > 0) {
					species.remove(i);
					i --;
				}
			}
		}
	}
	
	public void removeBadSpecies() {
		for(int i = 0; i < species.size(); i ++) {
			// Use the formula in the naturalSelection() method that allocates a proportion of children to the species to see if this species will even get a child
			if((species.get(i).avgFit / getAvgFitSum()) * population.size() < 1) {
				species.remove(i);
				i --;
			}
		}
	}
	
	public float getAvgFitSum() {
		// Basically returns the sum of all average fitness values in each species
		float avgSum = 0;
		for(Species s: species) {
			avgSum += s.avgFit;
		}
		
		return avgSum;
	}
	
	public void removeBottomHalf() {
		// Basically calls the removeBottomHalf method on all of the species
		for(Species s: species) {
			s.removeBottomHalf();
			
			// Re-calculate the average fitness since we just killed half of the species
			s.calculateAvgFitness();
		}
	}
	
	public void naturalSelection() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		// The most important method: the method that controls the creation of the next generation
		// Take a look at each method for more info, but the method names should give away what it does
		seperateIntoSpecies();
		calculateAllFitness();
		sortSpecies();
		
		// Before we start killing dinosaurs, we need to save the best player of this generation
		setBestPlayer();
		// Don't kill off species if the species size is less than or equal to 2
		if(species.size() > 2) {
			removeBottomHalf();
			removeStaleSpecies();
			removeBadSpecies();
		}
		
		// If we only have 2 or less species, add a new blank species
		if(species.size() <= 2) {
			species.add(new Species(new Dinosaur()));
		}
		
		// A new generation is born
		ArrayList<Dinosaur> newGeneration = new ArrayList<Dinosaur> ();
		
		// Add players based off the species
		for(int i = 0; i < species.size(); i ++) {
			// Don't do anything if the species' dinosaurs ArrayList size is 0 to prevent crashing
			if(species.get(i).dinosaurs.size() <= 0) {
				// Add the best player in each species
				newGeneration.add(species.get(i).bestPlayer.clone());
				
				// The amount of children this species is allowed
				int childrenSize = (int) (species.get(i).avgFit / getAvgFitSum() * population.size());
				
				// Start adding children with small mutations
				for(int j = 0; j < childrenSize; j ++) {
					// The History ArrayList will keep building as we add more mutations
					newGeneration.add(species.get(i).createOffspring(history));
				}
			}
		}
		
		// Because the cast to (int) floors values and may not hit the population size, we fill the rest of the population with babies from the best species
		while(newGeneration.size() < population.size()) {
			newGeneration.add(species.get(0).createOffspring(history));
		}
		
		// Remove any excess dinosaurs
		while(newGeneration.size() > population.size()) {
			newGeneration.remove(newGeneration.size() - 1);
		}
		
		// Clear the previous generation's population and replace it
		population.clear();
		population = newGeneration;
		generation ++;
		
		// Last thing we need to do is to link the nodes in the network for each of these new dinosaurs
		// and set them to be alive in case the program for some reason cloned the isDead = true variable
		for(int i = 0; i < population.size(); i ++) {
			population.get(i).n.linkNetwork();
			population.get(i).isDead = false;
		}
	}
	
	public void printInfo() {
		// Prints the info for each dinosaur's network
		for(int i = 0; i < population.size(); i ++) {
			System.out.println("Dinosaur " + i +": ");
			System.out.println("isDead: " + population.get(i).isDead);
		    population.get(i).n.printInfo();
		}
	}
}
