import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class AnimationOffsetPanel extends JPanel {
    Camera cam;
    AnimatedGameObject ago;
    AnimatedGameObject ghost;
    ArrayList<String> anims = new ArrayList<>();
    int curAnim = 0;
    ArrayList<int[]> offsets = new ArrayList<>();

    String copyString = "hit 'enter' to copy offsets to clipboard!";

    Timer updateTimer;
    Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(Main.TICK_TIME), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }

        });
        updateTimer.start();
    });

    private void attemptToLoadOffsets(String charName){
        File offsetFile = new File("./img/" + charName + "/offsets.txt");

        if(offsetFile.exists()){
            //System.out.println("file exists");
            Map<String, String> offsetList = new HashMap<>();
            Scanner offsetScanner = null;
            try {
                offsetScanner = new Scanner(offsetFile);
                while(offsetScanner.hasNextLine()){
                    String next = offsetScanner.nextLine();
                    offsetList.put(next.substring(0, next.indexOf(":")), next.substring(next.indexOf(":")+1));
                }

                for(int i = 0; i < anims.size(); i++){
                    String get = offsetList.get(anims.get(i));
                    int[] put = {Integer.parseInt(get.substring(0, get.indexOf(","))), Integer.parseInt(get.substring(get.indexOf(",")+1))};

                    offsets.add(i, put);
                }
                offsetScanner.close();
                ago.setPosition(offsets.get(curAnim)[0], offsets.get(curAnim)[1]);
                //System.out.println(offsets.get(curAnim)[0] + " " + offsets.get(curAnim)[1]);
            } catch (Exception e) {
                e.printStackTrace();
                if(offsetScanner != null) offsetScanner.close();
            }
        }
    }

    private Map<String, Boolean> getAnimationData(String charName){
        File animData = new File("./img/" + charName + "/animData.txt");
        Map<String, Boolean> ret = new HashMap<>();
        ret.put("has-arrow-poses", true);
        ret.put("has-alts", false);
        ret.put("has-misses", false);
        ret.put("has-ayy", false);
        ret.put("left-right-idle", false);

        if(animData.exists()){
            Scanner dataScanner = null;
            try{
                dataScanner = new Scanner(animData);
                while(dataScanner.hasNextLine()){
                    String next = dataScanner.nextLine();
                    ret.put(next.substring(0, next.indexOf(":")), Boolean.parseBoolean(next.substring(next.indexOf(":")+1)));
                }
            } catch(Exception e){
                e.printStackTrace();
                if(dataScanner != null) dataScanner.close();
            }
        }

        return ret;
    }

    private int getFrameData(String anim, String charName){
        File frameData = new File("./img/" + charName + "/frameData.txt");
        if(frameData.exists()){
            offsets.clear();
            Scanner frameScanner = null;
            int ret = 1;
            try{
                frameScanner = new Scanner(frameData);
                while(frameScanner.hasNextLine()){
                    String next = frameScanner.next();
                    String sub = next.substring(0, Math.min(anim.length(), next.length()));
                    if(sub.equals(anim)){
                        ret = Integer.parseInt(next.substring(next.indexOf(":")+1));
                        frameScanner.close();
                        return ret;
                    }
                }
                frameScanner.close();
                return ret;
            } catch(Exception e){
                e.printStackTrace();
                if(frameScanner != null) frameScanner.close();
            }
        }
        // we didnt find anything for it
        System.err.println("I DIDNT FIND ANYTHINGGGGGG");
        return 1;
    }

    public AnimationOffsetPanel(){
        cam = new Camera();
        cam.setCameraPos(-200, -200);
        ago = new AnimatedGameObject(0, 0, 1, cam);
        String charName = "dude";
        try{
            charName = Files.readString(Paths.get("./DUDE_CHAR_TO_LOAD.txt"));
        } catch(Exception e){
            e.printStackTrace();
        }
        Map<String, Boolean> data = getAnimationData(charName);
        boolean hasPoses = data.get("has-arrow-poses");
        boolean hasAlts = data.get("has-alts");
        boolean hasMisses = data.get("has-misses");
        boolean hasAyy = data.get("has-ayy");
        boolean leftRightIdle = data.get("left-right-idle");
        ago = new AnimatedGameObject(0, 0, 1, cam);
        if(hasPoses){
            ago.addAnimationFromSpritesheet("left", getFrameData("left", charName), "./img/"+charName+"/left.png");
            ago.addAnimationFromSpritesheet("down", getFrameData("down", charName), "./img/"+charName+"/down.png");
            ago.addAnimationFromSpritesheet("up", getFrameData("up", charName), "./img/"+charName+"/up.png");
            ago.addAnimationFromSpritesheet("right", getFrameData("right", charName), "./img/"+charName+"/right.png");
            anims.addAll(Arrays.asList("left", "down", "up", "right"));
        }
        if(hasMisses){
            ago.addAnimationFromSpritesheet("left-miss", getFrameData("left-miss", charName), "./img/"+charName+"/left-miss.png");
            ago.addAnimationFromSpritesheet("down-miss", getFrameData("down-miss", charName), "./img/"+charName+"/down-miss.png");
            ago.addAnimationFromSpritesheet("up-miss", getFrameData("up-miss", charName), "./img/"+charName+"/up-miss.png");
            ago.addAnimationFromSpritesheet("right-miss", getFrameData("right-miss", charName), "./img/"+charName+"/right-miss.png");
            anims.addAll(Arrays.asList("left-miss", "down-miss", "up-miss", "right-miss"));
        }
        if(hasAlts){
            ago.addAnimationFromSpritesheet("left-alt", getFrameData("left-alt", charName), "./img/"+charName+"/left-alt.png");
            ago.addAnimationFromSpritesheet("down-alt", getFrameData("down-alt", charName), "./img/"+charName+"/down-alt.png");
            ago.addAnimationFromSpritesheet("up-alt", getFrameData("up-alt", charName), "./img/"+charName+"/up-alt.png");
            ago.addAnimationFromSpritesheet("right-alt", getFrameData("right-alt", charName), "./img/"+charName+"/right-alt.png");
            anims.addAll(Arrays.asList("left-alt", "down-alt", "up-alt", "right-alt"));
        }
        if(hasAyy) {
            ago.addAnimationFromSpritesheet("ayy", getFrameData("ayy", charName), "./img/"+charName+"/ayy.png");
            anims.add("ayy");
        }
        if(!leftRightIdle){
            ago.addAnimationFromSpritesheet("idle", getFrameData("idle", charName), "./img/"+charName+"/idle.png");
            anims.add("idle");
        } else {
            ago.addAnimationFromSpritesheet("idle-left", getFrameData("idle-left", charName), "./img/"+charName+"/idle-left.png");
            ago.addAnimationFromSpritesheet("idle-right", getFrameData("idle-right", charName), "./img/"+charName+"/idle-right.png");
            anims.addAll(Arrays.asList("idle-left", "idle-right"));
        }

        ghost = new AnimatedGameObject(0, 0, 1, cam);
        if(!leftRightIdle){
            ghost.addAnimationFromSpritesheet("idle", getFrameData("idle", charName), "./img/"+charName+"/idle.png");
        } else {
            ghost.addAnimationFromSpritesheet("idle-left", getFrameData("idle-left", charName), "./img/"+charName+"/idle-left.png");
        }
        ghost.alpha = 0.4f;

        cam.addObjectToLayer("Objects", ago);

        cam.addObjectToLayer("Background", ghost);
        cam.addObjectToLayer("Objects", ago);

        for(int i = 0; i < anims.size(); i++){
            int[] arr = {0,0};
            offsets.add(i, arr);
        }
        attemptToLoadOffsets(charName);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0,false),
                "leftKeyPress");
            this.getActionMap().put("leftKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    moveGuy(-1, 0);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0,false),
                "rightKeyPress");
            this.getActionMap().put("rightKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    moveGuy(1, 0);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0,false),
                "upKeyPress");
            this.getActionMap().put("upKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    moveGuy(0, -1);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0,false),
                "downKeyPress");
            this.getActionMap().put("downKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    moveGuy(0, 1);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0,false),
                "changeAnimDown");
            this.getActionMap().put("changeAnimDown",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    curAnim++;
                    if(curAnim >= anims.size()) curAnim = 0;
                    ago.playAnimation(anims.get(curAnim));
                    int[] set = offsets.get(curAnim);
                    ago.setPosition(set[0], set[1]);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0,false),
                "changeAnimUp");
            this.getActionMap().put("changeAnimUp",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    curAnim--;
                    if(curAnim < 0) curAnim = anims.size()-1;
                    ago.playAnimation(anims.get(curAnim));
                    int[] set = offsets.get(curAnim);
                    ago.setPosition(set[0], set[1]);
				}

            }
        );
        
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0,false),
                "playAnim");
            this.getActionMap().put("playAnim",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    ago.playAnimation(anims.get(curAnim));
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
                "copyToClipboard");
            this.getActionMap().put("copyToClipboard",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    copyString = "copied!";
                    new Thread(()->{
                        try{Thread.sleep(1000);} catch(Exception ex){ex.printStackTrace();}
                        copyString = "hit 'enter' to copy offsets to clipboard!";
                    }).start();
                    String copy = "";
                    for(int i = 0; i < anims.size(); i++){
                        copy += anims.get(i)+":"+offsets.get(i)[0]+","+offsets.get(i)[1];
                        if(i != anims.size()-1) copy+="\n";
                    }
                    StringSelection selection = new StringSelection(copy);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
				}

            }
        );

        ago.playAnimation(anims.get(curAnim));
        updateThread.start();
    }

    private void moveGuy(double x, double y){
        System.out.println("eek");
        int[] set = offsets.get(curAnim);
        set[0] += x;
        set[1] += y;
        ago.setPosition(set[0], set[1]);
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        cam.drawViewport((Graphics2D) g);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
        g.setColor(Color.black);
        g.drawString(anims.get(curAnim) + " : [" + offsets.get(curAnim)[0] + ", " + offsets.get(curAnim)[1] + "]", 50, 50);
        g.drawString(copyString, 50, 700);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 22));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("use W and S to change animation and use arrow keys to move offsets", 50, 50+fm.getHeight()+15);
        g.drawString("hit 'space' to preview the animation", 50, 50+fm.getHeight()*2+15);
    }
}
