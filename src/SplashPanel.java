import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

// made with java!
public class SplashPanel extends JPanel {
    BufferedImage splash;
    //float alpha = 0f;

    Timer repaintTimer = new Timer(Main.TICK_TIME, (a)->{
        repaint();
        update();
    });

    public SplashPanel(){
        this.setBackground(Color.BLACK);
        try{
            splash = ImageIO.read(new File("./img/splash.png"));
        } catch(Exception e){
            e.printStackTrace();
        }
        repaintTimer.start();
        new Thread(()->{
            try{Thread.sleep(3000);} catch(Exception e){e.printStackTrace();}
            Main.main.goToStage();
        }).start();
    }

    private void update(){
        //alpha = Math.clamp(alpha + 0.05f, 0, 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        /*Composite comp = g2.getComposite();
        AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(alphaComp);*/
        g2.drawImage(splash, getWidth()/2-splash.getWidth()/2, getHeight()/2-splash.getHeight()/2, null);
        //g2.setComposite(comp);
    }
}
