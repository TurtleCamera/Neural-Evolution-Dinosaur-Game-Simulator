package DinosaurGame;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WhitePanel extends JPanel{
	
	public void paintComponent(Graphics g) {
		// Draws a white background (otherwise blank components will cause a gray color to appear on the screen)
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
	}
}
