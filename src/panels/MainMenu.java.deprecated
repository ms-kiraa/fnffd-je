import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

public class MainMenu extends JPanel {
    BufferedImage bg;
    BufferedImage[] choices = new BufferedImage[2];

    int selected = 0;
    Timer t;

    int bgWidth;
    int bgHeight;
    int choicesWidth;
    int choicesHeight;

    int y = 0;

    public MainMenu(){
        try{
            bg = ImageIO.read(new File("./img/ui/menu/main/spr_menubacksg_1.png"));
            bgWidth = bg.getWidth()*4;
            bgHeight = bg.getHeight()*4;

            choices[0] = ImageIO.read(new File("./img/ui/menu/main/spr_titlewords2alt_1.png"));
            choices[1] = ImageIO.read(new File("./img/ui/menu/main/spr_titlewords2alt_2.png"));

            // should be same size i think
            choicesWidth = (int)(choices[0].getWidth()*3.5);
            choicesHeight = (int)(choices[0].getHeight()*3.5);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    
        t = new Timer(15, (e) -> {
            repaint();
        });
        t.start();
    }

    public void addBinds(){
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
        "enter");
        this.getActionMap().put("enter",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(()->{
                    System.out.println("Hi i am");
                    //Game.extraClip.close();
                    FadeManager.cancelFade();
                    getActionMap().remove("enter");
                    SoundManager.playSFX("snd/snd_josh.wav");
                    FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                        switch(selected){
                            case 0:
                                // freeplay
                                //Main.main.transToGame();
                                break;
                            case 1:
                                // options (todo)
                                /*JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "not done yet", "sorry", JOptionPane.INFORMATION_MESSAGE);
                                System.exit(1);*/
                                //Main.main.goToOptions();
                                break;
                        }
                    });
                });
                t.start();
            }
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
        this.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected = Math.abs(selected - 1) % 2;
            }
        });
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
        this.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected = (selected + 1) % 2;
            }
        });
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        y+=((-60*4)*(selected)-y)/10;

        Graphics2D g2 = (Graphics2D) g.create();

        g2.drawImage(bg, 0, y, bgWidth, bgHeight, null);
        // choices requires centering. yay.
        g2.drawImage(choices[selected], (Main.windowWidth/2)-choicesWidth/2, (Main.windowHeight/2)-choicesHeight/2, choicesWidth, choicesHeight, null);

        if(FadeManager.fading) {
            FadeManager.drawSelf(g2);
        }

        g2.dispose();
    }
}
