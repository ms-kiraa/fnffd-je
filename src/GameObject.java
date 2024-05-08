import java.awt.*;
import java.awt.image.BufferedImage;



/**
 * The base object of which all other stage objects are derived. Pretty useless and does nothing on its own but can be extended for further functionality.
 */

public class GameObject {

    public double x;
    public double y;
    public double scale;

    public Rectangle bounds;
    public BufferedImage image;

    public GameObject(double x, double y, double scale, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.scale = scale;

        this.image = image;
        bounds = new Rectangle((int)x, (int)y, image.getWidth(), image.getHeight()); // todo: rounding x and y probably isnt the best solution??
    }

    protected void update(){

    }

    public void render(Graphics2D g) {
        g.drawImage(image, (int)x, (int)y, null); // todo: this doesnt take in account for the camera lol make it do that
    }

}
