package DinosaurGame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PaintPanel extends JPanel {
	final public static int inputX = (int)(1920 * (5.0 / 16.0)); // X coordinate for the input nodes to be drawn
	final public static int outputX = (int)(1920 * (11.0 / 16.0)); // X coordinate for the output nodes to be drawn
	public int bottomYBound; // Bottom y bound for the nodes to be drawn
	public int topYBound; // Top y bound for the nodes to be drawn
	final public static int halfNodeWidth = 16; // Half of the width of each circle for the node
	final public static int halfTextWidth = 3; // About a half of the width of each number for the node
	final public static int thirdTextHeight = 4; // About a third of the height of each number for the node
	final public static int halfTextHeight = 8; // About a half of the height of each number for the node
	final public static int spaceBetweenInputNodeText = 64; // Space between input node text
	final public static int spaceBetweenOutputNodeText = 130; // Space between output node text
	
	public static ArrayList<PaintNode> nodes = new ArrayList<PaintNode> ();
	
	// Draws all of the objects on the panel (which is added to the JFrame)
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
		if(Game.inGame) {
			g.setColor(Color.BLACK);
			// Draw the ground line
			g.fillRect(0, Game.ground.groundY, 1920, 2);
			
			// Draw the ground dots
			for(int i = 0; i < Game.ground.dots.size(); i ++) {
				g.fillRect(Game.ground.dots.get(i).x, Game.ground.dots.get(i).y, Game.ground.dots.get(i).w, 2);
			}
			
			// Draw the cacti
			for(int i = 0; i < Game.obstacles.size(); i ++) {
				g.drawImage(Game.obstacles.get(i).image, Game.obstacles.get(i).x, Game.obstacles.get(i).y - Game.obstacles.get(i).h, null);
			}
			
			// Draw the dinosaurs
			for(int i = 0; i < Game.pop.population.size(); i ++) {
				// Don't draw the ones that are dead
				if(!Game.pop.population.get(i).isDead) {
					g.drawImage(Dinosaur.currentImage, Game.pop.population.get(i).x, Game.pop.population.get(i).y - Game.pop.population.get(i).h, null);
				}
			}
			
			if(Game.state.equals("AI")) {
				// Draw a yellow box to mark where the best alive player is
				g.setColor(Color.RED);
				g.drawRect(Game.pop.population.get(Game.bestAlivePlayerIndex).x, Game.pop.population.get(Game.bestAlivePlayerIndex).y - Game.pop.population.get(Game.bestAlivePlayerIndex).h, Game.pop.population.get(Game.bestAlivePlayerIndex).w, Game.pop.population.get(Game.bestAlivePlayerIndex).h);
			}
			
			// Draw the scores
			g.setFont(new Font("SansSerif", Font.PLAIN, 27));
			g.setColor(Color.BLACK);
			g.drawString("Score: " + (int) Game.currentScore, 10, 1000);
			g.drawString("Best Score: " + Population.bestScore, 10, 1030);
			
			// Only draw these strings if we are in AI mode
			if(Game.state.equals("AI")) {
				// Draw the generation
				g.drawString("Generation: " + Game.pop.generation, 10, 970);
				
				// Draw how many species there are
				g.drawString("Species: " + Game.pop.species.size(), 10, 940);
				
				// Draw how many dinosaurs are alive
				g.drawString("Dinosaurs alive: " + Game.alivePlayerCount, 10, 910);
				
				// Draw the Dinosaur number that is displaying in the network
				g.drawString("Currently showing Dinosaur " + (Game.bestAlivePlayerIndex + 1) + "'s network", 10, 880);
				
				// Draw the text for each input node
				topYBound = (int) (1080 * (1.0 / 64.0));
				g.drawString("Bias", inputX - 85, topYBound + halfTextHeight);
				g.drawString("Distance until next cactus", inputX - 342, topYBound + halfTextHeight + spaceBetweenInputNodeText * 1);
				g.drawString("Height of next cactus", inputX - 284, topYBound + halfTextHeight + spaceBetweenInputNodeText * 2);
				g.drawString("Width of next cactus", inputX - 278, topYBound + halfTextHeight + spaceBetweenInputNodeText * 3);
				g.drawString("Speed", inputX - 109, topYBound + halfTextHeight + spaceBetweenInputNodeText * 4);
				g.drawString("Y Position", inputX - 153, topYBound + halfTextHeight + spaceBetweenInputNodeText * 5);
				g.drawString("Gap between next 2 cacti", inputX - 339, topYBound + halfTextHeight + spaceBetweenInputNodeText * 6);
				
				// Draw the text for each output node
				topYBound = (int) (1080 * (1.0 / 64.0)) + 130;
				g.drawString("Small Jump", outputX + 2 * halfNodeWidth + 3, topYBound + halfTextHeight);
				g.drawString("Big Jump", outputX + 2 * halfNodeWidth + 3, topYBound + halfTextHeight + spaceBetweenOutputNodeText);
			}
		
			// Draw the speed modifier (if the game is sped up or stopped)
			if(Game.speedMultiplier > 1) {
				g.drawString("Speed Modifier: X" + Game.speedMultiplier, 1650, 1030);
			}
			else if(Game.speedMultiplier <= 0) {
				g.drawString("Stopped", 1795, 1030);
			}
			

			if(Game.state.equals("AI")) {
				// Draw the best and alive player's network
				g.setFont(new Font("SansSerif", Font.PLAIN, 20));
				
				// Get Paint Nodes
				getPaintNodes();
				// Draw the connections first so they appear under the nodes
				for(int i = 0; i < Game.pop.population.get(Game.bestAlivePlayerIndex).n.c.size(); i ++) {
					PaintNode from = searchPaintNode(Game.pop.population.get(Game.bestAlivePlayerIndex).n.c.get(i).from.number);
					PaintNode to = searchPaintNode(Game.pop.population.get(Game.bestAlivePlayerIndex).n.c.get(i).to.number);
					
					// The thickness of the line doesn't exist in Graphics 1D
					Graphics2D g2D = (Graphics2D)g;
				    g2D.setStroke(new BasicStroke(determineThickness(Math.abs(Game.pop.population.get(Game.bestAlivePlayerIndex).n.c.get(i).weight))));
				    
				    // Set the color based on the sign of the weight
				    if(Game.pop.population.get(Game.bestAlivePlayerIndex).n.c.get(i).weight >= 0) {
				    	// Blue for positive weight
				    	g2D.setColor(Color.BLUE);
				    }
				    else {
				    	// Blue for negative weight
				    	g2D.setColor(Color.RED);
				    }
				    
				    // Now to draw the line
				    g2D.drawLine((int)from.nodePoint.getX() + halfNodeWidth, (int)from.nodePoint.getY() + halfNodeWidth, (int)to.nodePoint.getX() + halfNodeWidth, (int)to.nodePoint.getY() + halfNodeWidth);
				}
				
				// Draw each PaintNode
				for(int i = 0; i < nodes.size(); i ++) {
					g.setColor(Color.BLACK);
					g.fillOval((int)nodes.get(i).nodePoint.getX(), (int)nodes.get(i).nodePoint.getY(), 2 * halfNodeWidth, 2 * halfNodeWidth);
					g.setColor(Color.WHITE);
					g.drawString(nodes.get(i).numberInLayer + "", (int)nodes.get(i).textPoint.getX() - halfTextWidth, (int)nodes.get(i).textPoint.getY() + thirdTextHeight);	
				}
			}
		}
		
		repaint();
	}
	
	public int determineThickness(double weight) {
		// Returns a thickness value based on the weight
		if(weight >= 0.9) {
			return 20;
		}
		else if(weight >= 0.8) {
			return 18;
		}
		else if(weight >= 0.7) {
			return 16;
		}
		else if(weight >= 0.6) {
			return 14;
		}
		else if(weight >= 0.5) {
			return 12;
		}
		else if(weight >= 0.4) {
			return 10;
		}
		else if(weight >= 0.3) {
			return 8;
		}
		else if(weight >= 0.2) {
			return 6;
		}
		else if(weight >= 0.1) {
			return 4;
		}
		else {
			return 2;
		}
	}
	
	public PaintNode searchPaintNode(int number) {
		// Finds the number of this paint node in the ArrayList of paint nodes
		for(int i = 0; i < nodes.size(); i ++) {
			if(nodes.get(i).numberInNetwork == number) {
				return nodes.get(i);
			}
		}
		
		// We didn't find the node, but this should not be possible
		return null;
	}
	
	public void getPaintNodes() {
		// Creates all PaintNode objects and give the coordinates for them (including the location their number)
		nodes = new ArrayList<PaintNode> ();
		
		for(int j = 0; j < Game.pop.population.get(Game.bestAlivePlayerIndex).n.layers; j ++) {
			// Resize the bound for certain layers
			bottomYBound = (int)(1080 * (3.0 / 8.0));
			topYBound = (int)(1080 * (1.0 / 64.0));
			if(j == Game.pop.population.get(Game.bestAlivePlayerIndex).n.layers - 1) {
				bottomYBound -= 130;
				topYBound += 130;
			}
			else if(j > 0) {
				bottomYBound -= 80;
				topYBound += 80;
			}
			
			// Get the nodes in the layer
			ArrayList<Node> layerNodes = Game.pop.population.get(Game.bestAlivePlayerIndex).n.getNodesInLayer(j);
			int ySpacing = (int)(((bottomYBound - topYBound) / (double)(layerNodes.size() - 1)));
			// Space along the x axis between each layer
			int xSpacing = (int)((outputX - inputX) / (double)(Game.pop.population.get(Game.bestAlivePlayerIndex).n.layers - 1));
			
			// Deals with layers that only have 1 node
			int startingYPosition;
			if(layerNodes.size() == 1) {
				startingYPosition = (int) ((bottomYBound - topYBound) / 2.0);
			}
			else {
				startingYPosition = topYBound;
			}
			
			// Store the input nodes
			for(int i = 0; i < layerNodes.size(); i ++) {
				nodes.add(new PaintNode(inputX + xSpacing * j - halfNodeWidth, startingYPosition + ySpacing * i - halfNodeWidth, inputX + xSpacing * j - halfTextWidth, startingYPosition + ySpacing * i + thirdTextHeight, layerNodes.get(i).number, i));
			}
		}
	}
}
