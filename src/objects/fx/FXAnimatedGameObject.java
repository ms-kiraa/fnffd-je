package objects.fx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import backend.Camera;
import backend.imagemanip.ImageEffect;
import objects.AnimatedGameObject;

public class FXAnimatedGameObject extends AnimatedGameObject {
    ArrayList<ImageEffect> effects = new ArrayList<>();
    public FXAnimatedGameObject(double x, double y, double scale, Camera cam){
        super(x, y, scale, cam);
    }
    
    public void addEffect(ImageEffect effect){
        effects.add(effect);
        for(String anim : animations.keySet()){
            for(BufferedImage bi : animations.get(anim)){
                bi = effect.apply(bi);
            }
        }
    }

    @Override
    public void addAnimationFromSpritesheet(String name, int frames, String filepath){
        BufferedImage sheet;
        try{
            sheet = ImageIO.read(new File(filepath));
        } catch(Exception e){
            e.printStackTrace();
            System.out.println(filepath);
            return;
        }

        // calc width of each frame
        int frameWidth = sheet.getWidth()/frames;
        // and just get sheet height
        int sheetHeight = sheet.getHeight();

        // ok so this assumes that its just one row
        BufferedImage[] sprites = new BufferedImage[frames];
        for(int i = 0; i < frames; i++){
            BufferedImage frame = sheet.getSubimage(frameWidth*i + (name.equals("idle") && filepath.equals("./img/dude/idle.png") ? i : 0), 0, frameWidth, sheetHeight); // IM SO PISSED DUDE.
            for(ImageEffect ie : effects) {
                frame = ie.apply(frame);
            }
            sprites[i] = frame;
        }

        animations.put(name, new ArrayList<BufferedImage>(Arrays.asList(sprites)));

        this.image = sprites[0];
    }
}
