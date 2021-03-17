package DinosaurGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonListener implements ActionListener {
	// Listens for button presses
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("AI Self-Learning Mode")) {
			// Player selected AI mode
			Game.state = "AI";
		}
		else if (e.getActionCommand().equals("Normal Mode")) {
			// Player selected human mode
			Game.state = "human";
		}
		else if(e.getActionCommand().equals("Select dinosaur amount")) {
			// Player confirmed the amount of dinosaurs per generation
			Game.selectedDinosaurOption = (String)Game.options.prompt.getSelectedItem();
	
			// Set invisible so the user doesn't interact with it
			Game.options.setVisible(false);
		}
	}	
}