import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MultiImageGameObject extends GameObject {

    public ArrayList<BufferedImage> images = new ArrayList<>();
    private int displayedImage;

    public MultiImageGameObject(double x, double y, double scale, Camera cam, ArrayList<BufferedImage> images, int startingFrame) {
        super(x,y,scale,null,cam);
        this.images = images;
        this.displayedImage = Math.clamp(startingFrame, 0, images.size());
        setImage(images.get(displayedImage));
    }

    /**
     * Sets the displayed image to the next image in the list or the first in the list if the previous image was the last one.
     */
    public void displayNextImage(){
        displayedImage++;
        if(displayedImage >= images.size()) displayedImage = 0;

        setImage(images.get(displayedImage));
    }

    public void setDisplayedImage(int frame){
        displayedImage = Math.clamp(frame, 0, images.size());
    }

    public int getFrame(){
        return displayedImage;
    }
}
