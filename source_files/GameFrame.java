package DinosaurGame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class GameFrame extends JFrame implements KeyListener {
	
	@Override
	public void keyPressed(KeyEvent e) {
		// Jump only if we are in human controlled mode
		if(Game.state.equals("human")) {
			// Simulate big jump
			if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				try {
					Game.pop.population.get(0).bigJump();
				} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		// Speed up by adding 1 to speedMultiplier
		if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			// Limit the max speed multiplier to 10
			if(Game.speedMultiplier < 10) { 
				Game.speedMultiplier += 1;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			// Limit the min speed multiplier to 0
			if(Game.speedMultiplier > 0) { 
				Game.speedMultiplier -= 1;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Nothing needed
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Nothing needed
	}
}
