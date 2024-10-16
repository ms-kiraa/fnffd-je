package panels.editors;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JTextField;

//import org.json. // idk what i need yet

import backend.MusicBeatPanel;
import backend.FileRetriever;
import backend.data.cutscene.CutsceneFrameData;
import main.Main;

public class CutsceneEditorPanel extends MusicBeatPanel {
    public CutsceneEditorPanel(){super();}

    private ArrayList<CutsceneFrameData> frames = new ArrayList<>();

    BufferedImage ig;
    @Override
    protected void create(){
        ig = FileRetriever.image("songs/mus_tutorial/dialogue/spr_w0s1dialog1_11");
        super.create();
    }

    private void saveFrames() {
        for(CutsceneFrameData frame : frames) {
            // do something???
        }
    }

    private void drawUI(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // draw dialogue ui here

        // set alpha to draw background of ui
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        Composite prevAlpha = g2.getComposite();

        g2.setComposite(alpha);
        g2.setColor(Color.BLACK);

        // setup background vars and draw it
        int bgWidth = 500;
        int bgHeight = 250;

        g2.fillRect(Main.windowWidth - bgWidth, Main.windowHeight-bgHeight, bgWidth, bgHeight);

        g2.setComposite(prevAlpha);

        // set stroke to draw border
        Stroke prevStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(7, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        g2.setColor(Color.WHITE);
        g2.drawRect(Main.windowWidth-bgWidth, Main.windowHeight-bgHeight, bgWidth+50, bgHeight+50);

        g2.setStroke(prevStroke);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(ig, 0, 0, Main.windowWidth, Main.windowHeight, getFocusCycleRootAncestor());
        drawUI(g);
    }

}
