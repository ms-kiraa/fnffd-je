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
        bounds = new Rectangle();
        bounds.setRect(x, y, this.image.getWidth(), this.image.getHeight());
    }

    protected void update(){

    }

    public void render(Graphics2D g, Camera cam) {
        // todo: rounding probably isnt the best solution?? also no clue if the scale thing is right LOL
        g.drawImage(image, (int)(x-cam.x), (int)(y-cam.y), (int)(bounds.getWidth()*cam.scaleFactor), (int)(bounds.getHeight()*cam.scaleFactor), null);
    }

}
