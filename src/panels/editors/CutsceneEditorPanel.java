package panels.editors;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalButtonUI;

import org.json.JSONArray;
import org.json.JSONObject;

import backend.MusicBeatPanel;
import backend.FileRetriever;
import backend.data.cutscene.*;
import main.Main;

public class CutsceneEditorPanel extends MusicBeatPanel {
    public CutsceneEditorPanel(){super();}

    private int curFrame = 0;
    private ArrayList<CutsceneFrameData> frames;
    private Map<String, CutsceneCharacterData> chars = new HashMap<>();

    private JButton openConfig;
    private JButton addFrame;
    private JButton openFrameEvents;
    private JButton save;

    private JButton left;
    private JButton right;

    private JTextField charName;
    private JTextField bodyText;
    private JTextField frameImage;

    BufferedImage ig;
    BufferedImage textBox;

    private void makeCoolButton(AbstractButton button, String text) {
        // make weird lines not show up
        button.setFocusable(false);

        // make it look cool
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFont(new Font("Comic Sans MS", Font.BOLD, 15));

        button.setText("<html><center>" + text.replaceAll("\n", "<p>") + "</center></html>");

        // change "pressed" appearance from blue to gray
        button.setUI (new MetalButtonUI () {
            protected void paintButtonPressed (Graphics g, AbstractButton b) {
                if ( b.isContentAreaFilled() ) {
                    Dimension size = b.getSize();
                    g.setColor(Color.GRAY);
                    g.fillRect(0, 0, size.width, size.height);
                }
            }
        });

        // add it to the panel
        add(button);
    }

    private void makeCoolTextField(JTextField field) {
        field.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        field.setBorder(BorderFactory.createEmptyBorder());

        add(field);
    }
    @Override
    protected void create(){
        ig = FileRetriever.image("songs/mus_tutorial/dialogue/spr_w0s1dialog1_11");
        textBox = FileRetriever.image("img/ui/spr_textbox_0");
        
        frames = new ArrayList<>();
        frames.add(new CutsceneFrameData());

        setLayout(null);

        int dist = 120;

        openConfig = new JButton();
        openConfig.setBounds((Main.windowWidth-490)+(dist*0), Main.windowHeight-240, 110, 70);
        makeCoolButton(openConfig, "open cutscene config");

        addFrame = new JButton();
        addFrame.setBounds((Main.windowWidth-490)+(dist*1), Main.windowHeight-240, 110, 70);
        makeCoolButton(addFrame, "add cutscene frame");

        openFrameEvents = new JButton();
        openFrameEvents.setBounds((Main.windowWidth-490)+(dist*2), Main.windowHeight-240, 110, 70);
        makeCoolButton(openFrameEvents, "open frame events");

        save = new JButton();
        save.setBounds((Main.windowWidth-490)+(dist*3), Main.windowHeight-240, 110, 70);
        makeCoolButton(save, "save");

        left = new JButton();
        left.setBounds(Main.windowWidth-90, Main.windowHeight-75, 30, 30);
        makeCoolButton(left, "&lt;-");

        right = new JButton();
        right.setBounds(Main.windowWidth-55, Main.windowHeight-75, 30, 30);
        makeCoolButton(right, "-&gt;");

        charName = new JTextField();
        charName.setBounds((Main.windowWidth-490), Main.windowHeight-130, 200, 30);
        makeCoolTextField(charName);

        bodyText = new JTextField();
        bodyText.setBounds((Main.windowWidth-490), Main.windowHeight-75, 300, 30);
        makeCoolTextField(bodyText);

        frameImage = new JTextField();
        frameImage.setBounds(charName.getX()+charName.getWidth()+10, charName.getY(), charName.getWidth()+70, charName.getHeight());
        makeCoolTextField(frameImage);

        frameImage.addActionListener((a)->{
            BufferedImage newFrame = FileRetriever.image("songs/mus_tutorial/dialogue/" + frameImage.getText());
            if(newFrame == null) {
                newFrame = FileRetriever.image("img/unknownframe");
            }

            frames.get(curFrame).frameImg = newFrame;
        });

        super.create();
        saveFrames();
    }

    private void saveFrames() {
        /*for(CutsceneFrameData frame : frames) {
            // do something???
        }*/

        if(true) return;

        // set up entries
        JSONObject root = new JSONObject();
        JSONArray chars = new JSONArray();
        JSONArray frames = new JSONArray();

        JSONObject charTest = new JSONObject();
        charTest.put("internal_name", "test!!!!");
        charTest.put("hawk", "tuah");

        JSONObject charTest2 = new JSONObject();
        charTest2.put("test", "hi");
        charTest2.put("hi", "test");

        chars.put(charTest);
        chars.put(charTest2);

        root.put("def_characters", chars);

        try {
            Files.write(Paths.get("./json.json"), Arrays.asList(root.toString(4)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void drawUI(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // set alpha to draw background of ui and textbox
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        Composite prevAlpha = g2.getComposite();

        g2.setComposite(alpha);
        g2.setColor(Color.BLACK);

        // draw box
        int textboxX = Main.windowWidth/2-textBox.getWidth()/2;
        int textboxY = Main.windowHeight-textBox.getHeight()-20*3;
        g2.drawImage(textBox, textboxX, textboxY, null);

        // uughhh i hate this
        g2.setComposite(prevAlpha);
        // draw the body text
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString("null", textboxX+15+fm.getAscent()/4, textboxY+15+fm.getAscent());

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

        // draw curframe/numframes
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        String currentFrameText = "current frame:";
        String frameDisplay = curFrame+1 + "/" + frames.size();
        g2.drawString(currentFrameText, addFrame.getX(), addFrame.getY()+addFrame.getHeight()+15);
        g2.drawString(frameDisplay, (addFrame.getX()+addFrame.getWidth()/2)-fm.stringWidth(frameDisplay)/2, addFrame.getY()+addFrame.getHeight()+(int)(fm.getHeight()*1.5));

        // draw labels
        g2.drawString("character name", Main.windowWidth-490, Main.windowHeight-135);
        g2.drawString("body text", Main.windowWidth-490, Main.windowHeight-80);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(frames.get(curFrame).frameImg, 0, 0, Main.windowWidth, Main.windowHeight, getFocusCycleRootAncestor());
        drawUI(g);
    }

}
