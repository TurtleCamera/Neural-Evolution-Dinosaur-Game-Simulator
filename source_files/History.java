package DinosaurGame;

import java.util.ArrayList;

public class History {
	public Node from; // The Node that sends its output
	public Node to;	// The Node that receives the output from the from Node
	public int innovationNumber; // The nth connection (innovationNumber being n)
	public ArrayList<Integer> numberNetworkHistory; // All of the innovation numbers of connections that were present in the iteration of the network that had this new connection, allowing us to represent what the network looked like before

	public History(Node f, Node t, int n, ArrayList<Integer> numbers) {
		from = f;
		to = t;
		innovationNumber = n;
		numberNetworkHistory = new ArrayList<Integer> ();
		
		// There isn't a need to deep copy the list, but I'll do it just in case
		for(int i = 0; i < numbers.size(); i ++) {
			numberNetworkHistory.add(numbers.get(i));
		}
	}
	
	public boolean equals(Network n, Node from, Node to) {
		// Perform some checks before checking all connections in the network
		// Network should have the same amount of connections
		if(n.c.size() != numberNetworkHistory.size()) {
			return false;
		}
		
		// We are checking for the history that involved the from and to Node in this History object, so it needs to be the same
		if(!this.from.equals(from) || !this.to.equals(to)) {
			return false;
		}
		
		// Now check if every single connection in the network is in this History object (by checking the innovation number since each number was uniquely given)
		for(int i = 0; i < n.c.size(); i++) {
			if(numberNetworkHistory.contains(n.c.get(i).innovationNumber) == false) {
				return false;
			}
		}
		
		// All checks passed, so this is the same mutation
		return true;
	}
}
