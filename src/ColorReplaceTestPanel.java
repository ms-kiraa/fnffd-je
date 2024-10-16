import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;

public class ColorReplaceTestPanel extends MusicBeatPanel implements MouseListener {

    Camera cam;
    GameObject obj;
    FXGameObject arrow0;
    FXGameObject arrow1;
    FXGameObject arrow2;
    FXGameObject arrow3;
    public ColorReplaceTestPanel(){super();}

    Point ms;
    ArrayList<ColorReplaceEffect> colors;

    @Override
    protected void create(){
        if(ClientPrefs.noteColors != null) {
            colors = new ArrayList<>();
            colors.add(new ColorReplaceEffect(
                Arrays.asList(
                    Arrays.asList(172,116,180),
                    Arrays.asList(129,74,136),
                    Arrays.asList(0,0,0)
                ),
                Arrays.asList(
                    ClientPrefs.noteColors.get(0),
                    ClientPrefs.noteColors.get(1),
                    ClientPrefs.noteColors.get(2)
                )
            ));
            colors.add(new ColorReplaceEffect(
                Arrays.asList(
                    Arrays.asList(99,171,184),
                    Arrays.asList(66,133,145),
                    Arrays.asList(0,0,0)
                ),
                Arrays.asList(
                    ClientPrefs.noteColors.get(3),
                    ClientPrefs.noteColors.get(4),
                    ClientPrefs.noteColors.get(5)
                )
            ));
            colors.add(new ColorReplaceEffect(
                Arrays.asList(
                    Arrays.asList(109,182,128),
                    Arrays.asList(70,140,88),
                    Arrays.asList(0,0,0)
                ),
                Arrays.asList(
                    ClientPrefs.noteColors.get(6),
                    ClientPrefs.noteColors.get(7),
                    ClientPrefs.noteColors.get(8)
                )
            ));
            colors.add(new ColorReplaceEffect(
                Arrays.asList(
                    Arrays.asList(193,125,139),
                    Arrays.asList(142,68,83),
                    Arrays.asList(0,0,0)
                ),
                Arrays.asList(
                    ClientPrefs.noteColors.get(9),
                    ClientPrefs.noteColors.get(10),
                    ClientPrefs.noteColors.get(11)
                )
            ));
        }
        cam=new Camera();
        try {
            obj = new GameObject(Main.windowWidth/2, Main.windowHeight/2, 3, ImageIO.read(new File("./img/dude/skin-template.png")), cam);
            arrow0 = new FXGameObject(100, 100, 2, ImageIO.read(new File("./img/ui/notes/spr_notes_0.png")), cam);
            arrow0.addEffect(colors.get(0));
            arrow1 = new FXGameObject(200, 100, 2, ImageIO.read(new File("./img/ui/notes/spr_notes_1.png")), cam);
            arrow1.addEffect(colors.get(1));
            arrow2 = new FXGameObject(300, 100, 2, ImageIO.read(new File("./img/ui/notes/spr_notes_2.png")), cam);
            arrow2.addEffect(colors.get(2));
            arrow3 = new FXGameObject(400, 100, 2, ImageIO.read(new File("./img/ui/notes/spr_notes_3.png")), cam);
            arrow3.addEffect(colors.get(3));
        } catch (Exception e) {
            e.printStackTrace();
        }
        cam.addObjectToLayer("Objects", obj);
        cam.addObjectToLayer("Objects", arrow0);
        cam.addObjectToLayer("Objects", arrow1);
        cam.addObjectToLayer("Objects", arrow2);
        cam.addObjectToLayer("Objects", arrow3);
        obj.updateHitbox();
        arrow0.updateHitbox();
        addMouseListener(this);
        super.create();
    }

    @Override
    protected void update(float elapsed){
        ms = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(ms, this);
        //System.out.println(ms.getX() + " " + ms.getY());

        if(obj.bounds.contains(ms)) {
            if(obj.alpha != 0.2f) obj.alpha = 0.5f;
            //System.out.println("ohh yes");
        } else {
            obj.alpha = 1f;
        }
        super.update(elapsed);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        cam.drawViewport((Graphics2D) g);
        g.drawRect(obj.bounds.x, obj.bounds.y, obj.bounds.width, obj.bounds.height);
        FadeManager.drawSelf((Graphics2D) g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if(obj.bounds.contains(ms)) {
            obj.alpha = 0.2f;
        } else {
            obj.alpha = 1f;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(obj.bounds.contains(ms)) {
            obj.alpha = 0.5f;
            Color c = JColorChooser.showDialog(this, "GO", Color.WHITE, false);
            Color c1 = JColorChooser.showDialog(this, "GO", Color.WHITE, false);
            Color c2 = JColorChooser.showDialog(this, "GO", Color.WHITE, false);
            if(c != null && c1 != null) {
                if(arrow0.effects.size() > 0) arrow0.removeEffect(arrow0.effects.get(0));
                arrow0.addEffect(new ColorReplaceEffect(
                    Arrays.asList(
                        Arrays.asList(172,116,180),
                        Arrays.asList(129,74,136),
                        Arrays.asList(0,0,0)
                    ),
                    Arrays.asList(
                        Arrays.asList(c.getRed(), c.getGreen(), c.getBlue()),
                        Arrays.asList(c1.getRed(), c1.getGreen(), c1.getBlue()),
                        Arrays.asList(c2.getRed(), c2.getGreen(), c2.getBlue())
                    )
                ));
            }
            
        } else {
            obj.alpha = 1f;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
