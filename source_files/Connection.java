package DinosaurGame;

public class Connection {
	public Node from; // The Node that sends its output
	public Node to;	// The Node that receives the output from the from Node
	public double weight; // The weight of the output in this connection
	public boolean active; // Determines whether or not this connection is enabled
	public int innovationNumber; // The nth connection (innovationNumber being n)
	
	// Constructor
	public Connection(Node f, Node t, double w, int i) {
		from = f;
		to = t;
		weight = w;
		innovationNumber = i;
		active = true;
	}
	
	public void mutate() {
		// 10% of the time, completely change the weight
		if(Math.random() <= 0.1) {
			weight = random(false);
		}
		else {
			// Otherwise change it randomly using a normal distribution
			weight += gaussianCurve(random(false));
			
			// Bound the weight between -1 and 1
			if(weight < -1) {
				weight = -1;
			}
			
			if(weight > 1) {
				weight = 1;
			}
		}
	}
	
	public Connection clone(Node f, Node t) {
		// Return a semi-deep copy of the connection
		Connection c = new Connection(f, t, weight, innovationNumber);
		c.active = active;
		
		return c;
	}
	
	public double random(boolean whole) {
		// Returns either -1 or 1 if whole is true (as in whole number)
		// In later versions, I don't put true into this method
		double value = 1 - Math.random() * 2;
		if(whole) {
			if(value < 0) {
				return -1;
			}
			else {
				return 1;
			}
		}
		
		// Otherwise just return a double between -1 and 1
		return value;
	}
	
	public double gaussianCurve(double x) {
		// Returns a value based off a Gaussian (normal) curve with the peak value of the curve being 1
		double a = 0;
		double b = 0.3;
		double c = 0;
		
		// Determine whether or not a should be negative based on the sign of x
		if(x < 0) {
			a = -1;
		}
		else {
			a = 1;
		}
		
		return a * Math.pow(Math.E, -(Math.pow(x - c, 2) / (2 * Math.pow(b, 2))));
	}
}
