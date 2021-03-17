package DinosaurGame;

import java.util.ArrayList;

public class Network {
	public ArrayList<Connection> c = new ArrayList<Connection> (); // All of the connections of the nodes in this network
	public ArrayList<Node> n = new ArrayList<Node> (); // All of the nodes in this network (list the Nodes in order of bias Node --> input Nodes --> output Nodes --> new Nodes)
	public int inputNodes; // Amount of input nodes (will not change)
	public int outputNodes; // Amount of output nodes (will not change)
	public int layers; // Amount of layers in the network
	public int nodeCount; // Stores the Node number of the next Node that will be created
	public int biasPosition; // Position of the bias Node in the Node list
	public ArrayList<Node> genome = new ArrayList<Node> (); // The same as the Node arraylist above except that this will list Nodes in order of consideration of the network

	public Network(int i, int o) {
		inputNodes = i;
		outputNodes = o;
		layers = 2;
		nodeCount = 0;
		
		biasPosition = nodeCount;
		n.add(new Node(nodeCount));
		n.get(biasPosition).layer = 0;
		nodeCount ++;
		
		// Add input Nodes
		for(int j = 0; j < inputNodes; j ++) {
			n.add(new Node(nodeCount));
			n.get(j).layer = 0;
			nodeCount ++;
		}
		
		// Add ouput Nodes
		for(int j = 0; j < outputNodes; j ++) {
			n.add(new Node(nodeCount));
			n.get(j + inputNodes + 1).layer = 1;
			nodeCount ++;
		}
	}
	
	public Network(int i, int o, boolean placeHolder) {
		// Creates an empty Network
		// The only reason why placeHolder is there is just to differentiate this constructor from the other one
		inputNodes = i;
		outputNodes = o;
	}
	
	public int getExtraConnections(Network n) {
		// Count up the number of connections that exist on both Networks (based off innovation numbers)
		int matches = 0;
		
		for(int i = 0; i < c.size(); i ++) {
			for(int j = 0; j < n.c.size(); j ++) {
				if(c.get(i).innovationNumber == n.c.get(j).innovationNumber) {
					matches ++;
					// Check the next connection in this network immediately
					break;
				}
			}
		}
		
		// Return the # of extra connections (that is, excess/disjoint connections)
		return c.size() + n.c.size() - 2 * matches;
	}
	
	public int getAvgWeightDifference(Network n) {
		// Difference is 0 if either network has 0 connections
		if(n.c.size() == 0 || c.size() == 0) {
			return 0;
		}
		
		// Count up the number of connections that exist on both Networks (based off innovation numbers) and the sum of the differences of weights
		int matches = 0;
		int differenceSum = 0;
		
		for(int i = 0; i < c.size(); i ++) {
			for(int j = 0; j < n.c.size(); j ++) {
				if(c.get(i).innovationNumber == n.c.get(j).innovationNumber) {
					matches ++;
					differenceSum += Math.abs(c.get(i).weight - n.c.get(j).weight);
					// Check the next connection in this network immediately
					break;
				}
			}
		}
		
		if(matches == 0) {
			// Prevents divide by 0 error
			return 100;
		}
		else {
			return differenceSum / matches;
		}
	}
	
	public void generateConnections() {
		// Clear connections
		for (int i = 0; i < n.size(); i++) {
			n.get(i).c.clear();
		}

		// For each node, we link them again to form the network based off the connection ArrayList
		for (int i = 0; i < c.size(); i++) { 
			c.get(i).from.c.add(c.get(i));
		}
	}
	
	public Network clone() {
		// Return an almost deep copy of this Network
		// But make an empty network so we don't initialize duplicate input and output nodes
		Network temp = new Network(inputNodes, outputNodes, true);

		// Copy all of the nodes of this network
	    for (int i = 0; i < n.size(); i++) {
	      temp.n.add(n.get(i).clone());
	    }

	    // Now we copy the connections
	    for (int i = 0; i < c.size(); i++) {
	      temp.c.add(c.get(i).clone(temp.getNode(c.get(i).from.number), temp.getNode(c.get(i).to.number)));
	    }

	    temp.layers = layers;
	    temp.nodeCount = nodeCount;
	    temp.biasPosition = biasPosition;
	    temp.generateConnections();

	    return temp;
	}
	
	public Node getNode(int num) {
		// Does what you think it does
		for(int i = 0; i < n.size(); i ++) {
			if(n.get(i).number == num) {
				return n.get(i);
			}
		}
		
		return null;
	}
	
	public void connectNodes() {
		// Clear the connections stored in each node
		for(int i = 0; i < n.size(); i ++) {
			n.get(i).c.clear();
		}
		
		// Now we reconnect the nodes using the connection ArrayList in this class
		for(int i = 0; i < c.size(); i ++) {
			c.get(i).from.c.add(c.get(i));
		}
	}
	
	public void resetNetwork() {
		// Resets the inputTotal for the nodes in the Network (Genome ArrayList) for the feedInputs
		for(int i = 0; i < genome.size(); i++) {
			genome.get(i).inputTotal = 0;
		}
	}
	
	public float[] feedInputs(float[] inputs) {
		// Before we do anything, lets reset the values in the network
		resetNetwork();
		
		// The bias node output is always fixed at 1
		n.get(biasPosition).output = 1;
		
		// Feed the inputs to the input Nodes of the network (use i + 1 in n.get() because the first node is the bias node)
		for(int i = 0; i < inputNodes; i++) {
			n.get(i + 1).output = inputs[i];
		}
		
		// Now propagate the the rest of the network
		for(int i = 0; i < genome.size(); i++) {
			genome.get(i).sendOutputs();
		}
		
		// Get the outputs and return them
		float[] outputs = new float[outputNodes];
		for(int i = 0; i < outputNodes; i++) {
			outputs[i] = n.get(i + inputNodes + 1).output; // 1 to skip the bias node too
		}
		
		return outputs;
	}
	
	public void linkNetwork() {
		// Connect the nodes
		connectNodes();
		
		// Creates the network array with nodes in the order of importance when feeding forward (specifically by listing nodes in order of layers)
		genome = new ArrayList<Node> ();
		
		// For each layer, add all of the nodes in that layer
		for(int i = 0; i < layers; i ++) {
			for(int j = 0; j < n.size(); j ++) {
				if(n.get(j).layer == i) {
					genome.add(n.get(j));
				}
			}
		}
	}
	
	public int getRandomConnection() {
		return (int) (Math.random() * c.size()); 
	}
	
	public void newNode(ArrayList<History> h) {
		// Check if we even have connections in the first place
		if(c.size() == 0) {
			// If we don't even have a connection, make one
			newConnection(h);
		}
		else {
			// Get a random node
			int randomIndex = getRandomConnection();
			
			// We can disconnect if we have only 1 connection
			if(c.size() != 1) {
				// Get something that isn't the bias node
				while(c.get(randomIndex).from.equals(n.get(biasPosition))) {
					randomIndex = getRandomConnection();
				}
			}

			// Disable this connection
		    c.get(randomIndex).active = false;

		    // Create the new node
		    n.add(new Node(nodeCount ++));
		    
		    // Basically, what we are doing is taking a random connection and breaking int 2 connections (one from "from" to the new node and from the new node to "to")
		    // Get the innovation number of the new connection (from --> new node) we are creating (could be the a previous connection's innovation number)
		    int randomConnectionInnovationNumber = getInnovationNumber(h, c.get(randomIndex).from, getNode(nodeCount - 1));
		    // Make and add the new connection with a random weight
		    c.add(new Connection(c.get(randomIndex).from, getNode(nodeCount - 1), (float)(Math.random()), randomConnectionInnovationNumber));
		    
		    // Same thing, but with the connection from new node --> to
		    randomConnectionInnovationNumber = getInnovationNumber(h, getNode(nodeCount - 1), c.get(randomIndex).to );
		    // This time, we keep the weight of the disabled connection
		    c.add(new Connection(getNode(nodeCount - 1), c.get(randomIndex).to, c.get(randomIndex).weight, randomConnectionInnovationNumber));// Same thing, but with the connection from new node --> to
		    
		    // Same thing again, but this time we connect the bias node to the new node with a weight of 0
		    randomConnectionInnovationNumber = getInnovationNumber(h, getNode(biasPosition), getNode(nodeCount - 1));
		    // This time, we keep the weight of the disabled connection
		    c.add(new Connection(getNode(biasPosition), getNode(nodeCount - 1), 0, randomConnectionInnovationNumber));
		    
		    // The layer of this new node is the from node's layer + 1
		    getNode(nodeCount - 1).layer = c.get(randomIndex).from.layer + 1;
		    
		    // Create a new layer if the layer of this new node already exists
		    if (getNode(nodeCount - 1).layer == c.get(randomIndex).to.layer) {
		    	// Increment all nodes in front of the new node except for itself
		        for (int i = 0; i < n.size() - 1; i++) {
		        	if (n.get(i).layer >= getNode(nodeCount - 1).layer) {
		        		n.get(i).layer ++;
		        	}
		        }
		        
		        // Increment the total amount of layers
		        layers ++;
		    }
		    
		    // Reconnect all of the nodes
		    connectNodes();
		}
	}
	
	public void mutateWeights() {
    	for(int i = 0; i < c.size(); i ++) {
			c.get(i).mutate();
		}
	}
	
	public void newConnection(ArrayList<History> h) {
	    // Don't add a new connection if we are already fully connected (wait for a new node to be added)
	    if (fullyConnected()) {
	    	System.out.println("Network is fully connected");
	      
	    	// Just mutate all weights instead
	    	mutateWeights();
	    	return;
	    }

	    // Get 2 random nodes indexes
	    int index1 = (int) (Math.random() * n.size());
	    int index2 = (int) (Math.random() * n.size());
	    
	    // Reroll these 2 nodes until we get ones that are good
	    while (!isGoodConnection(index1, index2)) {
	      //get new ones
	      index1 = (int) (Math.random() * n.size());
	      index2 = (int) (Math.random() * n.size());
	    }
	    
	    // Let Node 1 be the first node (first as in lower layer)
	    int innovation;
	    if (n.get(index1).layer > n.get(index2).layer) {
	    	// Get a new innovation number for this connection unless there is already a Network that was mutated in the same way as the one that had this connection
	    	innovation = getInnovationNumber(h, n.get(index2), n.get(index1));
	    	// Generate a random weight
	    	c.add(new Connection(n.get(index2), n.get(index1), 1 - Math.random() * 2, innovation));
	    }   
	    else {
	    	// Same thing, but if Node 1 is at a lower layer
	    	innovation = getInnovationNumber(h, n.get(index1), n.get(index2));
	    	c.add(new Connection(n.get(index1), n.get(index2), 1 - Math.random() * 2, innovation));
	    }
	}
	
	public boolean isGoodConnection(int index1, int index2) {
		// Perform 2 checks
		// Connection isn't possible if on the same layer
		if (n.get(index1).layer == n.get(index2).layer) {
			return false;
		}
		
		// Don't use 2 nodes that are already connected
		if (n.get(index1).isConnectedTo(n.get(index2))) {
			return false;
		}
		
		return true;
	}
	
	public int findMutation(ArrayList<History> h, Node from, Node to) {
		// Determine whether or not the array list of histories contains this version of the connection of the from and to node in this version of the Network
	    for (int i = 0; i < h.size(); i++) {//for each previous mutation
	    	// Check if it's a match
	    	if (h.get(i).equals(this, from, to)) {
		        // If it's a match, then this new connection isn't a new mutation, so return the innovation number found
		        return h.get(i).innovationNumber; 
		    }
		}
	    
	    // Return -1 otherwise
	    return -1;
	}
	
	public int getInnovationNumber(ArrayList<History> h, Node from, Node to) {
	    int innovationNumber = findMutation(h, from, to);

	    // If we determine that this is a new connection/mutation, create a new History object to store this verison of the mutation + network
	    if (innovationNumber == -1) {
	    	// New ArrayList for the new version of the innovation numbers ArrayList that contains the innovation numbers of all of the existing connections + new connection
	    	ArrayList<Integer> numbers = new ArrayList<Integer>();
	    	for (int i = 0; i < c.size(); i++) {//set the innovation numbers
	    		numbers.add(c.get(i).innovationNumber);
	    	}

	    	// Create the new history object
	    	h.add(new History(from, to, h.size() + 1, numbers));
	      	
	    	// Return a new innovationNumber
	    	return h.size() + 1;
	    }
	    
    	// Otherwise return the already existing innovation number
    	return innovationNumber;
	}
	
	public int getMaxConnectionsPossible() {
		int connections = 0; // The count of all possible connections
		int [] nodesInEachLayer = new int[layers]; // Stores the amount of nodes in each layer
		
		// Count the number of nodes for each layer
		for(int i = 0; i < n.size(); i++) {
			nodesInEachLayer[n.get(i).layer] += 1;
		}
		
		// Formula for the amount of possible connections in each layer = # of nodes in layer * nodes in front of that layer
		// Add up all of the possible connections for each layer
		for(int i = 0; i < layers - 1; i ++) {
			// Amount of nodes in front of this layer
			int nodesAhead = 0;
			
			for(int j = i + 1; j < nodesInEachLayer.length; j ++) {
				nodesAhead += nodesInEachLayer[j];
			}
			
			// Use the formula and add to the total amount of possible connections
			connections += nodesInEachLayer[i] * nodesAhead;
		}
		
		return connections;
	}
	
	public boolean fullyConnected() {
		// If we have the  max possible connections, then it's fully connected
		if(getMaxConnectionsPossible() == c.size()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void mutate(ArrayList<History> h) {
		// If there isn't a connection in the first place, then add a connection only
		if(c.size() <= 0) {
			newConnection(h);
		}
		else {
			// Use RNG to determine what gets mutated
			// 80% chance to mutate all weights
			if(Math.random() <= 0.8) {
				mutateWeights();
			}
			
			// 5% chance to add a new connection
			if(Math.random() <= 0.05) {
				newConnection(h);
			}
			
			// 2% chance to add a new node
			if(Math.random() <= 0.02) {
				newNode(h);
			}
		}
	}
	
	public Network cloneForBreeding() {
		// Create a network, but set it up for the purpose of breeding the Network of 2 parents
		Network network = new Network(inputNodes, outputNodes);
	    network.c.clear();
	    network.n.clear();
	    network.layers = layers;
	    network.nodeCount = nodeCount;
	    network.biasPosition = biasPosition;
	    
	    return network;
	}
	
	public Network breedNetwork(Network parent) {
		Network network = cloneForBreeding();
		
		// Stores the connections between the two parents, but with more emphasis on this network
	    ArrayList<Connection> childConnections = new ArrayList<Connection> ();
	    // Stores whether or not the connection corresponding to the above ArrayList are enabled
	    ArrayList<Boolean> enabledConnections = new ArrayList<Boolean>(); 
	    
	    // Start checking to see which connections are enabled
	    for (int i = 0; i< c.size(); i++) {
	    	boolean enabled = true;

	    	// See if the parameter parent contains the connection innovation number from this parent
	    	int parameterParentConnection = matchingConnection(parent, c.get(i).innovationNumber);
	    	
	    	// If the connection exists in both parents 
	    	if (parameterParentConnection != -1) {
	    		// Check if either of the parent's connections are disabled
	    		if (!c.get(i).active || !parent.c.get(parameterParentConnection).active) {
	    			// Use RNG to determine whether or not the connection will be disabled based on who has their connection disabled
		    		if(!c.get(i).active && !parent.c.get(parameterParentConnection).active) {
			    		// 80% chance that the connection is disabled if the connections in both parents are disabled
			    		if (Math.random() < 0.8) {
			    			enabled = false;
			    		}
		    		}
		    		else if(!c.get(i).active && parent.c.get(parameterParentConnection).active) {
		    			// 60% chance that the connection is disabled if the connection in only the dominant parent is disabled
		    			if(Math.random() < 0.6) {
		    				enabled = false;
		    			}
		    		}
		    		else if(c.get(i).active || !parent.c.get(parameterParentConnection).active) {
		    			// 30% chance that the connection is disabled if the connection in only the weaker parent is disabled
		    			if(Math.random() < 0.3) {
		    				enabled = false;
		    			}
		    		}
	    		}
	    		
	    		// 65% chance that we get the connection from the dominant parent
	    		if (Math.random() < 0.65) {
	    			childConnections.add(c.get(i));
	    		}
	    		else {
	    			childConnections.add(parent.c.get(parameterParentConnection));
	    		}	
	    	}
	    	else {
	    		// This is an excess/disjoint connection, but we will still store it since it's the dominant parent's connection
	    		childConnections.add(c.get(i));
	    		enabled = c.get(i).active;
	    	}
	    	
	    	enabledConnections.add(enabled);
	    }

	    // Because we put emphasis on the connection of the dominant parent, the structure of the network is the same, so we can copy all of the nodes from the dominant parent
	    for (int i = 0; i < n.size(); i++) {
	    	network.n.add(n.get(i).clone());
	    }

	    // Clone all of the connections too
	    for ( int i = 0; i < childConnections.size(); i++) {
	    	network.c.add(childConnections.get(i).clone(network.getNode(childConnections.get(i).from.number), network.getNode(childConnections.get(i).to.number)));
	    	network.c.get(i).active = enabledConnections.get(i);
	    }

	    // Connect and return the new child's network
	    network.connectNodes();
	    return network;
	}
	

	public int matchingConnection(Network parent, int innovationNumber) {
		// This basically checks to see if the Node in the dominant parent (the innovation number of the node) exists in the parameter parent
		for (int i = 0; i < parent.c.size(); i++) {
			// Check to see if the innovation numbers match
			if (parent.c.get(i).innovationNumber == innovationNumber) {
				return i;
			}
	    }
		
		// Connection not found
	    return -1;
	}
	
	public ArrayList<Node> getNodesInLayer(int layer) {
		// Returns an ArrayList that contains all of the nodes in one layer (used for the paint panel)
		ArrayList<Node> nodes = new ArrayList<Node> ();
		
		for(int i = 0; i < n.size(); i ++) {
			// If the node is in the given layer
			if(n.get(i).layer == layer) {
				nodes.add(n.get(i));
			}
		}
		
		return nodes;
	}
	
	public void printInfo() {
		// Just some debug information of the Network (final version won't call this method)
	    System.out.println("Network layers: " + layers);  
	    System.out.println("Bias node position: " + biasPosition);
	    System.out.println("Node numbers:");
	    for (int i = 0; i < n.size(); i++) {
	    	System.out.print(n.get(i).number + ", ");
	    }
	    System.out.println();
	    System.out.println("Connection innovation numbers:");
	    for (int i = 0; i < c.size(); i++) { 
	    	System.out.println("Connection " + c.get(i).innovationNumber + "   From: " + c.get(i).from.number + "   To: " + c.get(i).to.number + "   Active: " + c.get(i).active + "   From node layer: " + c.get(i).from.layer + "   To node layer: " + c.get(i).to.layer + "   Weight: " + c.get(i).weight);
	    }

	    System.out.println();
	}
}
