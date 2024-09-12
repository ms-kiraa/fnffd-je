import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class TitleScreen extends JPanel {
    BufferedImage bg;
    int bgWidth = 0;
    int bgHeight = 0;

    BufferedImage logo;
    int logoWidth = 0;
    int logoHeight = 0;

    BufferedImage enter;
    int enterWidth = 0;
    int enterHeight = 0;

    BufferedImage javaDude;
    int dudeWidth = 0;
    int dudeHeight = 0;

    int width = Main.windowWidth;
    int height = Main.windowHeight;

    Thread redrawThread = new Thread(()->{
        Timer t = new Timer(15, (e)->{
            repaint();
        });
        t.start();
    });

    float draws = 0;

    /**
     * @deprecated succeeded by TitlePanel
     * @see TitlePanel
     */
    public TitleScreen(){
        SoundManager.loopExtraSound("snd/mus_game.wav"); // start song
        try{
            // load the stuf!
            bg = ImageIO.read(new File("./img/ui/menu/main/spr_menubacksg_0.png"));
            bgWidth = bg.getWidth()*4;
            bgHeight = bg.getHeight()*4;

            logo = ImageIO.read(new File("./img/ui/menu/main/spr_title_0.png"));
            logoWidth = logo.getWidth()*4;
            logoHeight = logo.getHeight()*4;

            enter = ImageIO.read(new File("./img/ui/menu/main/spr_titlewords_0.png"));
            enterWidth = enter.getWidth()*4;
            enterHeight = enter.getHeight()*4;

            javaDude = ImageIO.read(new File("./img/ui/menu/main/java.png"));
            dudeWidth = (int)(javaDude.getWidth()*2.5);
            dudeHeight = (int)(javaDude.getHeight()*2.5);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
        "enter");
        this.getActionMap().put("enter",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(()->{
                    getActionMap().remove("enter");
                    FadeManager.fadeIn(Color.WHITE, 1, 1);
                    SoundManager.playSFX("snd/snd_josh.wav");
                    try{Thread.sleep(1000);} catch(InterruptedException ex){ex.printStackTrace();}
                    FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                        Main.main.goToMainMenu();
                    });
                });
                t.start();
            }
        });

        redrawThread.start();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draws+=0.05;

        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(bg, -200*((bgWidth/bg.getWidth())), 0, bgWidth, bgHeight, null);
        g2.drawImage(logo, (width/2)-logoWidth/2, 20, logoWidth, logoHeight, null);
        g2.drawImage(javaDude, (width/2)-dudeWidth/2, (int)((height-(dudeHeight-7))+-Math.abs(Math.sin(draws))*15), dudeWidth, dudeHeight, null);

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float)Math.abs(Math.sin(draws)));
        g2.setComposite(ac);
        g2.drawImage(enter, 0, height - (int)(enterHeight*1.5), enterWidth, enterHeight, null); // this is stupid
        ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1);
        g2.setComposite(ac);



        if(FadeManager.fading) FadeManager.drawSelf(g2);
    }
}
