package objects.fx;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import backend.Camera;
import backend.imagemanip.ImageEffect;
import objects.GameObject;

public class FXGameObject extends GameObject {
    protected ArrayList<ImageEffect> effects = new ArrayList<>();

    public FXGameObject(double x, double y, double scale, BufferedImage image, Camera cam) {
        super(x, y, scale, image, cam);
    }

    public void addEffect(ImageEffect effect){
        effects.add(effect);
        image = effect.apply(image);
    }

    public void removeEffect(ImageEffect effect) {
        effects.remove(effect);
        image = effect.remove(image);
    }

}
