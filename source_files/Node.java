package DinosaurGame;

import java.util.ArrayList;

public class Node {
	int number; // Marks the nth node (n being number)
	float inputTotal = 0; // The sum of all outputs from the nodes connected to this node as inputs
	float output = 0; // What this node outputs
	  
	ArrayList<Connection> c = new ArrayList<Connection>(); // All of the connections that this node goes out to
	int layer = 0; // The layer that this Node is on
	  
	//constructor
	public Node(int no) {
		number = no;
	}

	// Sends out the output of this Node to the connected nodes, taking into account weights
	public void sendOutputs() {
		// Calculate the output using the inputs if we are not on the first layer
		if (layer != 0) {
			output = sigmoid(inputTotal);
	    }

		// For the input of each Node that this Node is connected to, add the weighted output value of this Node to the input
	    for (int i = 0; i < c.size(); i++) {
	    	// Only do this for enabled connections though
	    	if (c.get(i).active) {
	    		c.get(i).to.inputTotal += c.get(i).weight * output;
	    	}
	    }
	}
	
	// The sigmoid function used to translate the numbers to a value in the range of -1 to 1
	public float sigmoid(float x) {
		return (float) (1 / (1 + Math.pow((float) Math.E, -4.9 * x)));
	}
	  
	// Checks if this node has a connection to the parameter node (this function is called when finding new connections to create)
	public boolean isConnectedTo(Node n) {
		// Nodes on the same layer are never connected
		if (n.layer == layer) {
			return false;
	    }

	    // Checks if the toNode of Node n is this node
	    for (int i = 0; i < n.c.size(); i++) {
	    	if (n.c.get(i).to.equals(this)) {
	    		return true;
	    	}
	    }

	    // Same thing, but the other way around
	    for (int i = 0; i < c.size(); i++) {
	    	if (c.get(i).to.equals(n)) {
	    		return true;
	    	}
	    }
	    
	    // Neither is the toNode of the other
	    return false;
	}
	  
	//returns a copy of this node
	public Node clone() {
		// Deep copy
		Node clone = new Node(number);
	    clone.layer = layer;
	    
	    return clone;
	}
	
	// Overriding the equals() method
	public boolean equals(Node n) {
		return number == n.number;
	}
}
