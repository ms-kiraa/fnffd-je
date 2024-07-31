import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Stage extends JPanel {
    Camera cam;

    Timer redrawTimer;
    int draws = 0;

    // very very barebones! this is all just for testing tho

    public Stage(){
        cam = new Camera(); // testing
        
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        try{
            bi = ImageIO.read(new File("./img/stages/w1/houseback1.png"));
        } catch(Exception e){}

        //ArrayList<String> layers = new ArrayList<>(Arrays.asList("Objects", "Background", "UI"));
        
        /*for(int i = 0; i < 500; i++) {
            GameObject obj = new GameObject(Math.round(Math.random()*800), Math.round(Math.random()*800), 1, bi, cam);

            cam.addObjectToLayer(layers.get((int)(Math.random() * layers.size())), obj);
        }*/

        GameObject hb1 = new GameObject(0, 0, 1, bi, cam);
        try{
            bi = ImageIO.read(new File("./img/stages/w1/houseback2.png"));
        } catch(Exception e){}
        GameObject hb2 = new GameObject(-50, 30, 1, bi, cam);
        hb2.scrollFactor = 0.7;
        cam.setBackground(new Color(145, 207, 221));
        cam.addObjectToLayer("Background", hb2);
        cam.addObjectToLayer("Background", hb1);

        UINote uin = new UINote(10, 10, Note.Direction.DOWN, cam, false, true);
        cam.addObjectToLayer("UI", uin);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0,false),
                "d");
            this.getActionMap().put("d",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					uin.visPress();
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0,false),
                "j");
            this.getActionMap().put("j",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cam.moveCameraX(-5);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0,false),
                "i");
            this.getActionMap().put("i",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cam.moveCameraY(-5);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0,false),
                "l");
            this.getActionMap().put("l",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cam.moveCameraX(5);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0,false),
                "k");
            this.getActionMap().put("k",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cam.moveCameraY(5);
				}

            }
        );

        redrawTimer = new Timer(15, (e) -> {
            repaint();
            draws++;
        });

        redrawTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //cam.moveCamera(-1, -1);
        cam.drawViewport((Graphics2D) g);
        //cam.changeCameraZoom(-0.005);
        //cam.moveCamera(Math.cos(draws/5)*10, Math.sin(draws/5)*10);
    }
}
