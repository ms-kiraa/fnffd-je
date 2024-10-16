package panels;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import backend.Conductor;
import backend.MusicBeatPanel;
import backend.data.SongData;
import backend.save.ClientData;
import backend.imagemanip.ImageUtils;
import backend.managers.*;
import main.Main;

public class FreeplayPanel extends MusicBeatPanel {
    SongData[] songs;
    BufferedImage bg;
    public FreeplayPanel(){super();}

    double mod = 0;
    double textMod = 0;
    int targ = 0;
    int targTextMod = 0;
    int curSel = 0;

    int textInc = (int)(51*1.7);

    float targH;
    float curH;

    double bgMod = 0;
    int bgTarg = 0;
    int bgInc = 10;

    boolean queueMode = false;
    String queueModeText = "QUEUE MODE | (Q to add, E to remove, R to reset queue)";
    ArrayList<String> queue = new ArrayList<>();
    Map<String, ArrayList<Integer>> queueNums = new HashMap<>();
    Color selQueueColor = Color.YELLOW;
    Color nonselQueueColor = new Color(188, 127, 22);

    BufferedImage bgBase;

    Font bigFont = new Font("Comic Sans MS", Font.PLAIN, 24);
    Font smallFont = new Font("Comic Sans MS", 0, 20);

    private double lerp(double a, double b, double f){
        return (a * (1.0 - f)) + (b * f);
    }
    private float lerpf(float a, float b, float f){
        return (float)((a * (1.0 - f)) + (b * f));
    }
    private Color getRainbowAlongRange(){
        float interval = 1f/(songs.length);
        targH = interval*curSel;
        //System.out.println(interval*curSel);
        return Color.getHSBColor(curH, 0.6f, 0.75f);
    }
    @Override
    protected void create(){
        try{
            bgBase = ImageIO.read(new File("./img/ui/menu/main/spr_menubacksg_3.png"));
        }catch(Exception e){
            e.printStackTrace();
        }
        songs = SongData.loadAllSongs();
        bg = ImageUtils.colorMultiply(bgBase, getRainbowAlongRange());
        super.create();
        FadeManager.fadeIn(Color.BLACK, 1, 1);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
        this.getActionMap().put("up", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(curSel-1<0) return;
                targ += Main.windowHeight/2;
                targTextMod += textInc;
                bgTarg += bgInc;
                curSel--;
                bg = ImageUtils.colorMultiply(bgBase, getRainbowAlongRange());
            }
            
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
        this.getActionMap().put("down", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(curSel+1>=songs.length) return;
                targ -= Main.windowHeight/2;
                targTextMod -= textInc;
                bgTarg -= bgInc;
                curSel++;
                bg = ImageUtils.colorMultiply(bgBase, getRainbowAlongRange());
            }
            
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "enter");
        this.getActionMap().put("enter", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SoundManager.songClip.loop(0);
                    SoundManager.songClip.stop();
                    if(!queueMode) {
                        Files.write(Paths.get("./SONG_TO_LOAD_NAME.txt"), Arrays.asList(songs[curSel].fileName));
                    } else {
                        Files.write(Paths.get("./SONG_TO_LOAD_NAME.txt"), queue);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Main.main.goToStage();
            }
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0, false), "toggleQueue");
        this.getActionMap().put("toggleQueue", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                queueMode = !queueMode;
                if(!queueMode) {
                    SoundManager.playSFX("./snd/snd_queuemodeleave.wav");
                    queue.clear();
                    queueNums.clear();
                } else {
                    SoundManager.playSFX("./snd/snd_queuemodeenter.wav");
                }
            }
        });

        // queue keys
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, false), "addQueue");
        this.getActionMap().put("addQueue", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(queueMode) {
                    queue.add(songs[curSel].fileName);
                    if(!queueNums.keySet().contains(songs[curSel].fileName)) {
                        queueNums.put(songs[curSel].fileName, new ArrayList<>());
                    }
                    queueNums.get(songs[curSel].fileName).add(queue.size());
                    System.out.println(queueNums.get(songs[curSel].fileName));
                    SoundManager.playSFX("./snd/snd_queueadd.wav");
                }
            }
        });
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "removeQueue");
        this.getActionMap().put("removeQueue", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(queueMode && queue.contains(songs[curSel].fileName)) {
                    ArrayList<Integer> list = queueNums.get(songs[curSel].fileName);
                    int removed = list.get(list.size()-1);
                    list.remove(list.size()-1);
                    System.out.println(queueNums.get(songs[curSel].fileName));
                    for(ArrayList<Integer> numlist : queueNums.values()) {
                        for(int num : numlist) {
                            if(num >= removed) {
                                System.out.println("shifted " + num + " down one");
                                int id = numlist.indexOf(num);
                                numlist.set(id, numlist.get(id)-1);
                            }
                        }
                    }
                    queue.remove(queue.indexOf(songs[curSel].fileName));
                    SoundManager.playSFX("./snd/snd_queueremove.wav");
                }
            }
        });
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "resetQueue");
        this.getActionMap().put("resetQueue", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                queue.clear();
                queueNums.clear();
                if(queueMode) {
                    SoundManager.playSFX("./snd/snd_queuereset.wav");
                }
            }
        });
    }

    @Override
    protected void update(float elapsed){
        super.update(elapsed);

        if(Math.abs(mod-targ) > 1) mod = lerp(mod, targ, 0.1);
        else mod = targ;
        if(Math.abs(textMod-targTextMod) > 1) textMod = lerp(textMod, targTextMod, 0.2);
        else textMod = targTextMod;

        if(curH != targH) {
            bg = ImageUtils.colorMultiply(bgBase, getRainbowAlongRange());
        }
        curH = lerpf(curH, targH, 0.2f);
        mod = lerp(mod, targ, 0.2);
        bgMod = lerp(bgMod, bgTarg, 0.2);
    }

    private void drawOutlinedText(Graphics g, String str, int x, int y, int thickness) {
        Color originalColor = g.getColor();
        g.setColor(Color.BLACK);
    
        // Draw the outline around the text by spreading the text horizontally and vertically
        for (int i = -thickness; i <= thickness+1; i++) {
            for (int j = -thickness; j <= thickness+2; j++) {
                if (i != 0 || j != 0) {  // Ensure that we don't draw the text at (0, 0), which would overlap the main text
                    g.drawString(str, x + i, y + j);
                }
            }
        }
    
        // Restore the original color and draw the main text
        g.setColor(originalColor);
        g.drawString(str, x, y);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(bg, -800, (int)bgMod, 800*2, (800/bg.getWidth())*bg.getHeight()*2, null);
        Graphics2D g2 = (Graphics2D) g;
        Composite ac = g2.getComposite();
        g2.setColor(getRainbowAlongRange());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.fillRect(0, 0, Main.windowWidth, Main.windowHeight);
        g2.setComposite(ac);
        g.setColor(Color.WHITE);
        for(int i = 0; i < songs.length; i++){
            String formalName = songs[i].formalName;
            if(i == curSel) {
                if(queue.contains(songs[i].fileName)) g.setColor(selQueueColor);
                else g.setColor(Color.WHITE);
            }
            else {
                if(queue.contains(songs[i].fileName)) g.setColor(nonselQueueColor);
                else g.setColor(Color.GRAY);
            }
            if(queue.contains(songs[i].fileName)) {
                String indexes = "";
                ArrayList<Integer> numlist = queueNums.get(songs[i].fileName);
                for(int j = 0; j < numlist.size()-1; j++) {
                    indexes += numlist.get(j) + ", ";
                }
                indexes += numlist.get(numlist.size()-1);
                formalName = "(" + indexes + ") " + formalName;
            }
            g.setFont(bigFont);
            drawOutlinedText(g, formalName, 70, (int)(textMod+(Main.windowHeight/2))+(textInc)*i, 3);
            g.setFont(smallFont);
            int imgY = (int)(mod+(Main.windowHeight/2-(songs[i].icon.getWidth()*2)/2))+(Main.windowWidth/2)*i;
            if(imgY <= 800 && imgY >= 0-songs[i].icon.getHeight()*2) {
                g.setColor(Color.BLACK);
                int outlineThickness = 2;
                g.fillRect(450-outlineThickness, imgY-outlineThickness, (songs[i].icon.getWidth()*2)+outlineThickness*2, (songs[i].icon.getHeight()*2)+outlineThickness*2);
                if(i == curSel) {
                    g.drawImage(songs[i].icon, 450, imgY, (int)(songs[i].icon.getWidth()*2), (int)(songs[i].icon.getHeight()*2), null);
                } else {
                    g.drawImage(ImageUtils.colorMultiply(songs[i].icon, new Color(100, 100, 100)), 450, imgY, (int)(songs[i].icon.getWidth()*2), (int)(songs[i].icon.getHeight()*2), null);
                }
                if(i == curSel) {
                    if(queue.contains(songs[i].fileName)) g.setColor(selQueueColor);
                    else g.setColor(Color.WHITE);
                }
                else {
                    if(queue.contains(songs[i].fileName)) g.setColor(nonselQueueColor);
                    else g.setColor(Color.GRAY);
                }
                int[] data = ClientData.getSongScore(songs[i]);
                drawOutlinedText(g, "Score: " + (data == null ? 0 : data[0]) + " | Misses: " + (data == null ? 0 : data[1]), 450, imgY+(songs[i].icon.getWidth()*2+20), 2);
            }
        }

        if(queueMode) {
            g.setColor(selQueueColor);
            g.setFont(smallFont);
            FontMetrics fm = g.getFontMetrics();
            drawOutlinedText(g, queueModeText, Main.windowWidth/2-fm.stringWidth(queueModeText)/2, Main.windowHeight-50+(int)(Math.sin(Conductor.getStepDb())*3), 3);
        }
        FadeManager.drawSelf(g2);
        g2.dispose();
    }
}
