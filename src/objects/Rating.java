package objects;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import backend.Camera;
import backend.managers.SoundManager;

public class Rating extends GameObject {
    public static final double gravity = 0.16;
    public static final float fadeRate = 0.02f;
    
    public double xvel = 0;
    public double yvel = 0;

    private boolean bumped = false;
    private double growRate = 0.025;

    public boolean active = true;

    public Rating(double x, double y, BufferedImage img, Camera cam) {
        super(x, y, 0.5, img, cam);
    }

    @Override
    public void update(){
        yvel += gravity;
        y += yvel;
        x += xvel;

        scale += growRate;
        if(scale > 0.7) bumped = true;
        if(bumped && growRate > -0.025) {
            growRate -= 0.005;
        }

        alpha -= fadeRate;
        if(alpha <= 0) {
            active = false;
            alpha = 0;
        }
    }

    @Override
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
        g.drawImage(img, (int) (imgPosX-image.getWidth()*scale/2), (int) (imgPosY-image.getHeight()*scale/2), (int) ((img.getWidth()*scale) * cam.scaleFactor), (int) ((img.getHeight()*scale) * cam.scaleFactor), null);
        g.setComposite(pre);
        if(drawHitbox) g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
