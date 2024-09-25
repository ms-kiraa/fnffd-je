import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class FXGameObject extends GameObject {
    ArrayList<ImageEffect> effects = new ArrayList<>();

    public FXGameObject(double x, double y, double scale, BufferedImage image, Camera cam) {
        super(x, y, scale, image, cam);
    }

    public void addEffect(ImageEffect effect){
        effects.add(effect);
        image = effect.apply(image);
    }

}
