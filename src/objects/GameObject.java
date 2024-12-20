package objects;

import java.awt.*;
import java.awt.image.BufferedImage;

import backend.Camera;

/**
 * The base object of which all other stage objects are derived. Can be extended for further functionality.
 * <p>
 * Effectively just a BufferedImage but with okay-ish hitboxes.
 */

public class GameObject {

    public double x;
    public double y;
    public double scale;
    public double scrollFactor = 1;
    public boolean camScaleAffectsScale = true;
    public double camScaleAffectAmount = 1;

    public boolean visible = true;
    public boolean drawHitbox = false;
    public boolean ignoreCamScale = false;

    public Rectangle bounds;
    public BufferedImage image;

    public float alpha = 1;

    public Camera cam;

    public int drawID = -1;

    public GameObject(double x, double y, double scale, BufferedImage image, Camera cam) {
        this.x = x;
        this.y = y;
        this.scale = scale;

        this.image = image;
        this.cam = cam;
        bounds = new Rectangle();
        recalculateBounds();
    }

    /**
     * Sets the displayed image to the specified image and recalculates the bounds.
     * @param image image to display
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        recalculateBounds();
    }

    /**
     * Moves the object onscreen and recalculates its bounds
     * @param x new X position
     * @param y new Y position
     */
    public void setPosition(double x, double y){
        this.x = x;
        this.y = y;

        recalculateBounds();
    }

    /**
     * Sets the scale of the object and recalculates its bounds
     * @param scale new scale
     */
    public void setScale(double scale){
        this.scale = scale;
        recalculateBounds();
    }

    /**
     * Check if the hitbox of this GameObject intersects the hitbox of another GameObject
     * @param otherObject GameObject to check
     */
    public boolean intersects(GameObject otherObject){
        return bounds.intersects(otherObject.bounds);
    }

    protected void update(){
        // WIP
    }

    /**
     * Draws the object's image to the screen.
     * @param g
     * @param cam
     */
    public void render(Graphics2D g, Camera cam) {
        render(g, cam, image);
    }

    protected void render(Graphics2D g, Camera cam, BufferedImage img) {
        if(!visible) return;
        // Calculate the center of the camera
        //double camCenterX = cam.x + cam.getBounds().width / 2.0;
        //double camCenterY = cam.y + cam.getBounds().height / 2.0;
    
        // Calculate the position of the image relative to the center of the camera
        double imgPosX = ((x - (cam.x*scrollFactor)) * (ignoreCamScale ? 1 : cam.scaleFactor)/* + cam.getBounds().width / 2.0*/);
        double imgPosY = ((y - (cam.y*scrollFactor)) * (ignoreCamScale ? 1 : cam.scaleFactor)/* + cam.getBounds().height / 2.0*/);
    
        // Draw the image with the new calculated positions
        // the stupid zoom doesnt work very well but thats ok ill barely use it
        Composite pre = g.getComposite();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g.setComposite(ac);
        g.drawImage(img, (int) imgPosX, (int) imgPosY, (int) ((img.getWidth()*scale) * cam.scaleFactor), (int) ((img.getHeight()*scale) * cam.scaleFactor), null);
        g.setComposite(pre);
        if(drawHitbox) g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void recalculateBounds(){
        bounds.setRect((x+(cam.x-cam.getBounds().width/2)*cam.scaleFactor), (y+(cam.y-cam.getBounds().height/2)*cam.scaleFactor), (this.image.getWidth()*scale)*cam.scaleFactor, (this.image.getHeight()*scale)*cam.scaleFactor);
        bounds.setLocation((int)(((x)-(cam.x)*scrollFactor)*cam.scaleFactor), (int)(((y)-(cam.y)*scrollFactor)*cam.scaleFactor));
    }

    public void updateHitbox(){
        recalculateBounds();
    }

}
