package DinosaurGame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class OptionBox extends JFrame{
	public String[] options = {"10", "25", "50 (Default)", "75", "100 (May have slight lag)", "150 (May have slight lag)", "200 (May have considerable lag)", "250 (May have considerable lag)", "500 (Strong computer required)", "750 (Not recommended)", "1000 (Not recommended)"}; // Options for the user
	public JComboBox<String> prompt; // The drop down menu for the user
	
	public OptionBox() {
		// Set up the frame and combo box
		prompt = new JComboBox<String>((String[]) options);
		setLayout(new GridLayout(2, 1));
		setSize(300, 120);
		setTitle("Quantity");
		
		// Set the location of this frame in the center of the screen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(d.width / 2 - getSize().width / 2, d.height / 2 - this.getSize().height / 2);
		
		// Don't let the user close this window, otherwise the program will never get an option
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		// Add listener and set the default to 50
		prompt.setSelectedIndex(2);
		this.add(prompt);
		
		// Add a button to confirm the selection
		JButton start = new JButton("Select dinosaur amount");
		start.addActionListener((ActionListener) new ButtonListener());
		this.add(start);
		validate();
	}
}
