import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Stage extends JPanel {
    Camera cam;

    // very very barebones! this is all just for testing tho

    public Stage(){
        cam = new Camera(); // testing
        
        
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        try{
            bi = ImageIO.read(new File("./img/characters/dude/ayy/spr_dudeayy_0.png"));
        } catch(Exception e){}

        ArrayList<String> layers = new ArrayList<>(Arrays.asList("Objects", "Background", "UI"));
        
        for(int i = 0; i < 100000; i++) {
            GameObject obj = new GameObject(Math.round(Math.random()*800), Math.round(Math.random()*800), 50, bi);

            cam.addObjectToLayer(layers.get((int)(Math.random() * layers.size())), obj);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        cam.drawViewport((Graphics2D) g);
    }
}
