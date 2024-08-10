import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class AnimatedGameObject extends GameObject {
    
    public Map<String, ArrayList<BufferedImage>> animations = new HashMap<>();
    /**
     * Time between each "tick" of animation
     */
    int frameTimeMS = 120;
    Thread animationThread;
    
    public AnimatedGameObject(double x, double y, double scale, Camera cam){
        super(x, y, scale, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), cam);
    }

    public void addAnimationFromSpritesheet(String name, int frames, String filepath){
        BufferedImage sheet;
        try{
            sheet = ImageIO.read(new File(filepath));
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
        // calc width of each frame
        int frameWidth = sheet.getWidth()/frames;
        // and just get sheet height
        int sheetHeight = sheet.getHeight();

        // ok so this assumes that its just one row
        BufferedImage[] sprites = new BufferedImage[frames];
        for(int i = 0; i < frames; i++){
            sprites[i] = sheet.getSubimage(frameWidth*i, 0, frameWidth, sheetHeight);
        }

        animations.put(name, new ArrayList<BufferedImage>(Arrays.asList(sprites)));

        this.image = sprites[0];
        /*Thread temp = new Thread(()->{
            try{Thread.sleep(3000);} catch(Exception e){e.printStackTrace();}
            System.out.println("playing anim");
            playAnimation(name);
        });*/
        //temp.start();
    }

    public void playAnimation(String animation){
        if(animationThread != null) animationThread.interrupt();
        animationThread = new Thread(()->{
            int i = 0;
            ArrayList<BufferedImage> anim = animations.get(animation);
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
        // todo: change thread name to make debugging easier
        animationThread.start();
    }

}
