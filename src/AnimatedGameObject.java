import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class AnimatedGameObject extends GameObject {
    
    public Map<String, ArrayList<BufferedImage>> animations = new HashMap<>();
    public Map<String, int[]> offsets = new HashMap<>();
    /**
     * Time between each "tick" of animation
     */
    int frameTimeMS = 120;
    Thread animationThread;

    double xx, yy;
    
    public AnimatedGameObject(double x, double y, double scale, Camera cam){
        super(x, y, scale, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), cam);
        xx = x;
        yy = y;
    }

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
            sprites[i] = sheet.getSubimage(frameWidth*i + (name.equals("idle") && filepath.equals("./img/dude/idle.png") ? i : 0), 0, frameWidth, sheetHeight); // IM SO PISSED DUDE.
        }

        animations.put(name, new ArrayList<BufferedImage>(Arrays.asList(sprites)));

        this.image = sprites[0];
    }

    public boolean hasAnimation(String name){
        return animations.containsKey(name);
    }

    public void addOffset(String name, int x, int y){
        int[] arr = {x, y};
        offsets.put(name, arr);
    }

    @Override
    public void setPosition(double x, double y){
        super.setPosition(x, y);
        xx = x;
        yy = y;
    }

    public void playAnimation(String animation){
        if(animationThread != null) animationThread.interrupt();
        animationThread = new Thread(()->{
            int i = 0;
            ArrayList<BufferedImage> anim = animations.get(animation);
            if(offsets.containsKey(animation)) {
                int[] set = offsets.get(animation);
                x = xx + set[0];
                y = yy + set[1];
            }
            while(i < anim.size()){
                this.image = anim.get(i);
                //System.out.println("showing frame " + i);
                i++;
                try{
                    Thread.sleep(frameTimeMS);
                } catch (Exception e){
                    //e.printStackTrace();
                    break;
                }
            }
        });
        animationThread.setName("Animation "+animation+" Thread");
        animationThread.start();
    }

}
