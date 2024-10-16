package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

import backend.managers.*;
import main.Main;

public class RecordScratchScreen extends JPanel {
    BufferedImage bi;
    int biWidth;
    int biHeight;
    Timer t;
    boolean show = false;

    public RecordScratchScreen(){
        try{
            bi = ImageIO.read(new File("./img/ui/menu/main/spr_bing_0.png"));
            biWidth = bi.getWidth()*4;
            biHeight = bi.getHeight()*4;
        } catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }

        t = new Timer(15, (e) ->{
            repaint();
        });

        t.start();

        // time for something stupid
        Thread th = new Thread(() ->{
            try{
                Thread.sleep(250);
                SoundManager.playSFX("./snd/snd_recordscratch.wav");
                show = true;
                Thread.sleep(750);
                show = false;
                Thread.sleep(500);
                FadeManager.fadeIn(Color.WHITE, 1, 1);
                Main.main.goToTitlePanel();
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        th.start();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 800);

        if(show) g.drawImage(bi, 0, 0, biWidth, biHeight, null);
    }


}
