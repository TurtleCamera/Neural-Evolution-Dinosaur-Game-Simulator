package DinosaurGame;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Species {
	ArrayList<Dinosaur> dinosaurs; // Players in this species
	float bestFit; // The best player's fitness
	Dinosaur bestPlayer; // The player with the best fitness
	float avgFit = 0; // Average fitness of the dinosaurs in this species
	int stale = 0;// # of generations that this species didn't improve
	Network representation; // Representation of the network that had this species

	Species(Dinosaur d) {
		// Takes in a player and uses it as the basis for this species
		bestFit = 0;
		avgFit = 0;
		stale = 0;
		
		dinosaurs = new ArrayList<Dinosaur>();
		dinosaurs.add(d); 
		
		// Make this player the best player since it's the only one in the species
		bestFit = d.fitness; 
		representation = d.n.clone();
		bestPlayer = d.clone();
	}
	
	// Based off the excess connections and the difference in weights, return true if the parameter network is similar enough
	public boolean similarSpecies(Network n) {
		final float coefficientExcess = 1.0f; // Coefficient for excess connections
		final float coefficientWeightDifference = 0.5f; // Coefficient for eeight difference
		final float threshold = 3; // Threshold for the compatibility (if the compatibility is higher than 1, then it's not similar enough)
		
		float extraConnections = n.getExtraConnections(representation);//get the number of excess and disjoint genes between this player and the current species rep
		float avgWeightDifference = n.getAvgWeightDifference(representation);//get the average weight difference between matching genes
	
		float networkReduction; // Reduces the # of connections by 20 for the sake of checks 
		if (n.c.size() - 20 < 1) {
			networkReduction = 1;
		}
		else {
			networkReduction = n.c.size() - 20;
		}
	
		float compatibility =  ((coefficientExcess * extraConnections) / networkReduction) + (coefficientWeightDifference * avgWeightDifference); // compatablilty formula
		return threshold > compatibility;
	}
	
	public void add(Dinosaur d) {
		// Does what you think it does
		dinosaurs.add(d.clone());
	}
	
	public void sortDinosaurs() {
		// Staleness is maximum if there are no players
		if(!selectionSort()) {
			stale ++;
			return;
		}
		
	    // Check and replace if we have a new best player
	    if (dinosaurs.get(0).fitness > bestFit) {
	    	stale = 0; // Stale = 0 because we finally improved
	    	bestFit = dinosaurs.get(0).fitness;
	    	representation = dinosaurs.get(0).n.clone();
	    	bestPlayer = dinosaurs.get(0).clone();
	    }
	    else {
	    	// Otherwise increase staleness since we didn't improve
	    	stale ++;
	    }
	}
	
	public boolean selectionSort() {
		// Besides sorting, this method will also return false if the size is 0 (which automatically means it's stale)
		if(dinosaurs.size() == 0) {
			return false;
		}

		for(int i = 0; i < dinosaurs.size(); i ++) {
			int highestFitnessPosition = i;
			
			for(int j = i; j < dinosaurs.size(); j ++) {
				if(dinosaurs.get(j).fitness > dinosaurs.get(highestFitnessPosition).fitness) {
					highestFitnessPosition = j;
				}
			}
			
			Dinosaur temp = dinosaurs.get(i);
			dinosaurs.set(i, dinosaurs.get(highestFitnessPosition));
			dinosaurs.set(highestFitnessPosition, temp);
			
		}
		
		return true;
	}
	
	public void calculateAvgFitness() {
		// Does what you think it does
		float sum = 0.0f;
		for(int i = 0; i < dinosaurs.size(); i ++) {
			sum += dinosaurs.get(i).fitness;
		}
		
		avgFit = sum / dinosaurs.size();
	}
	
	public Dinosaur createOffspring(ArrayList<History> h) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		// 90% chance that the baby is just a clone of a "almost random" player (see fitnessRandomSelect() for more info)
		Dinosaur d;
		if(Math.random() < 0.9) {
			d = fitnessRandomSelect().clone();
		}
		else {
			// 10% chance that we use that fitnessRandomSelect() to breed two parents
			Dinosaur parent1 = fitnessRandomSelect();
			Dinosaur parent2 = fitnessRandomSelect();
			
			// The "this" of the breedParents method call will be the dominant parent
			if(parent1.fitness < parent2.fitness) {
				d = parent1.breedParents(parent2);
			}
			else {
				d = parent2.breedParents(parent1);
			}
		}
		
		// Mutate the network of the offspring as well before returning it
		d.n.mutate(h);
		return d;
	}
	
	public int fitnessSum() {
		// Does what you think it does
		int fitnessSum = 0;
		for(int i = 0; i < dinosaurs.size(); i ++) {
			fitnessSum += dinosaurs.get(i).fitness;
		}
		
		return fitnessSum;
	}
	
	public Dinosaur fitnessRandomSelect() {
		// Selects a Dinosaur in which dinosaurs with higher fitness have a higher chance to be selected
		int weightedRandom = (int)(Math.random() * fitnessSum());
		
		int totalFitness = 0;
		for(int i = 0; i < dinosaurs.size(); i ++) {
			totalFitness += dinosaurs.get(i).fitness;
			
			// The "weighted" check
			if(totalFitness >= weightedRandom) {
				return dinosaurs.get(i);
			}
		}
		
		// Should not be reachable, but if we for some reason do reach this point, then just select a random dinosaur
		return dinosaurs.get((int)(Math.random() * dinosaurs.size())).clone();
	}
	
	public void removeBottomHalf() {
		// Kills the worst half of the species
		int threshold = dinosaurs.size() / 2;

		for(int i = 0; i < threshold; i ++) {
			dinosaurs.remove(dinosaurs.size() - 1);
		}
	}
}
