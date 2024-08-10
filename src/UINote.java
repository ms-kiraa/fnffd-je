import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.util.*;

import javax.imageio.ImageIO;

public class UINote extends Note {
    
    private boolean pressed = false;
    public static int presses = 0;

    public UINote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, direction, camera, fump, playerNote);
        this.playerNote = playerNote;
        setImage();
    }

    public UINote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, direction, camera, fump, playerNote);
        this.playerNote = playerNote;
        setImage();
    }

    private void setImage(){
        try{
            String path = (fumpNote) ? ("fump/spr_uinotefump_") : ("spr_uinotes_");
            path += this.dir.getDirectionAsInt();
            BufferedImage bi;
            if(cache.containsKey(path)) bi = cache.get(path);
            else {
                bi = ImageIO.read(new File("./img/ui/notes/ui/" + path + ".png"));
                cache.put(path, bi);
            }
            setImage(bi);
        } catch(Exception e){e.printStackTrace(); System.exit(1);}
    }

    public void visPress(){
        presses++;
        pressed = true;
        String threadName = (playerNote ? "Player" : "BadGuy") + this.dir.getDirectionAsString(CapsMode.UPPER_CAMEL_CASE) + "PressThread" + presses;
        Thread pressThread = new Thread(()->{
            try{Thread.sleep(200);}catch(Exception e){e.printStackTrace();}
            pressed = false;
        });
        pressThread.setName(threadName);
        pressThread.start();
    }

    @Override
    public void render(Graphics2D g, Camera cam) {
        AlphaComposite oldAC = (AlphaComposite) g.getComposite();
        if(pressed){
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g.setComposite(ac);
        }
        super.render(g, cam);
        if(pressed) g.setComposite(oldAC);
    }
}
