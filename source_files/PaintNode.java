package DinosaurGame;

import java.awt.Point;

public class PaintNode {
	public Point nodePoint; // Center of the node's position
	public Point textPoint; // Center of the text's position
	
	public int numberInNetwork; // Number of this node in the neural network
	public int numberInLayer; // Number of this node in its specific layer
	
	// Constructor
	public PaintNode(int nodeX, int nodeY, int textX, int textY, int numNetwork, int numLayer) {
		nodePoint = new Point(nodeX, nodeY);
		textPoint = new Point(textX, textY);
		
		numberInNetwork = numNetwork;
		numberInLayer = numLayer;
	}
}
