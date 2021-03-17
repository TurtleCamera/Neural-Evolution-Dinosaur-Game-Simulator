package DinosaurGame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Cactus {
	public int x; // The leftmost hitbox of the cactus
	public int y; // The bottom hitbox of the cactus
	public int h; // Height of the cactus
	public int w; // Width of the cactus

	public int type; // Type of the cactus

	public BufferedImage image; // Image of the cactus

	public Cactus() throws IOException {
		// Randomly generate the type of cactus
		type = (int) (Math.random() * 3);

		// Get the parameters for this type of cactus
		switch (type) {
		case 0: // small cactus (Cactus 1)
			w = 40;
			h = 80;
			image = ImageIO.read(new File("cactus1.png"));
			break;
		case 1: // Big cactus (Cactus 2)
			w = 60;
			h = 120;
			image = ImageIO.read(new File("cactus2.png"));
			break;
		case 2: // Small cacti (Cactus 3)
			w = 120;
			h = 80;
			image = ImageIO.read(new File("cactus3.png"));
			break;
		}

		x = 1920 * 2; // Start the cactus far off screen to the right
		y = (int) (1080 * (3.0 / 4.0)) + 8; // The bottom of the cactus is at the level of the floor
	}

	public boolean collisionDetection(int playerX, int playerY, int playerW, int playerH) {
		// Variables that mark the boundaries of the player
		int pLeft = playerX;
		int pRight = playerX + playerW;
		int pBottom = playerY;

		// Variables that mark the boundaries of the cactus
		int cLeft = x;
		int cRight = x + w;
		int cTop = y - h;

		// If the bounds of the player are touching or in the cactus, then return true
		if ((pLeft <= cRight && pRight >= cLeft) || (cLeft <= pRight && cRight >= pLeft)) {
			if (pBottom >= cTop) {
				// Remember, the y axis is flipped
				return true;
			}
		}

		// False if the player didn't collide with this cactus
		return false;
	}

	public boolean updateLocation(int speedVector) {
		x -= speedVector;
		
		// While we are here, lets see if the cactus is out of bounds
		return (x + w) < 0;
	}
}
