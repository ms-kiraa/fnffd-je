import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Stage extends JPanel {
    Camera cam;

    final int targetFPS = 60;
    final int TICK_TIME = 1000/targetFPS;

    int draws = 0;

    int event = 0;
    int misses = 0;

    String songName;
    int bpm;
    double scrollSpeed;
    double songlong;
    double songbeat;
    double songpos;

    long lastTickMillis = System.currentTimeMillis();

    ArrayList<ArrayList<GameNote>> curChart;
    ArrayList<ArrayList<Integer>> curChartTypes;
    Map<String, Map<Integer, UINote>> uiNotes = new HashMap<>();

    public static Stage instance;

    boolean[] keysPressed = {false, false, false, false};

    Clip song;

    AnimatedGameObject ago;

    Timer updateTimer;
    Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(TICK_TIME), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                update();
                repaint();
                draws++;
                long elapsed = System.currentTimeMillis() - lastTickMillis;
                //int currentFPS = 1000 / (int)elapsed;
                //System.out.println("TIME SINCE LAST TICK: " + elapsed + " ms\nDIFFERENCE FROM TARGET: " + (elapsed - TICK_TIME) + " ms\nCURRENT FPS: " + currentFPS);
                lastTickMillis = System.currentTimeMillis();
            }

        });
        updateTimer.start();
    });

    private GameNote findHittableNoteInLane(boolean playerLane, int lane){
        //System.out.println(lane + (playerLane ? 4 : 0));
        ArrayList<GameNote> laneList = curChart.get(lane + (playerLane ? 4 : 0));
        UINote ui = uiNotes.get(playerLane ? "Player" : "BadGuy").get(lane%4);
        int range = 40;

        for(GameNote note : laneList) {
            if(note.y >= ui.y - range && note.y <= ui.y + range && !note.autohit && !note.hit){
                return note;
            }
        }
        return null;
    }
    
    // find what event to execute and do it
    private void doEvent(){
        //System.out.println("executing event " + event);
        switch(songName){
            case "mus_w4s2":
                switch(event){
                    case 0:
                        System.out.println("PLAYING SFX\\");
                        SoundManager.playSFX("./snd/snd_firework.wav");
                        break;
                }
                break;
        }
        event++;
    }

    private void removeNote(GameNote gn){
        if(gn.isHold()) cam.removeObjectFromLayer("UI hold", gn);
        else cam.removeObjectFromLayer("UI", gn);
        gn = null;
    }

    private void update(){
        songpos = (SoundManager.songClip.getMicrosecondPosition() / 1000000.0);
        double songProgress = songpos / songlong;
        double ymod = (48 + songProgress * songbeat);

        ListIterator<ArrayList<GameNote>> iter1 = curChart.listIterator();
        while (iter1.hasNext()){
            ListIterator<GameNote> iter2 = iter1.next().listIterator();
            while(iter2.hasNext()){
                GameNote gn = iter2.next();
                gn.move(ymod);
                if(gn.y < 0-gn.image.getHeight()) {
                    removeNote(gn);
                    iter2.remove();
                    if(!gn.hit && !gn.autohit) {
                        System.out.println("possible miss? how will the economy recover");
                        misses++;
                    }
                    continue;
                }

                if(gn.autohit){
                    //if(Math.round(Math.random()*5) == 2) Main.s = new Stage();
                    if(gn.y <= uiNotes.get("Player").get(3).y) {
                        if(gn.type == GameNote.NoteType.EVENT) {
                            System.out.println("dats an event note right htere.");
                            doEvent();
                        } else if(gn.type == GameNote.NoteType.END_SONG_TRIGGER){
                            System.out.println("EJNDING SONG!!");
                            //redrawTimer.stop();
                            //updateThread.interrupt();
                            Main.s = new Stage();
                        }
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }
                }

                if(gn.playerNote){
                    // handle player note stuff
                    if( 
                        (gn.isHold()) && 
                        keysPressed[gn.dir.getDirectionAsInt()] && 
                        gn.y <= uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y &&
                        !gn.autohit
                    ){
                        playAnim(gn);
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }

                    if(gn.autohit && gn.y <= uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y){

                    }
                } else {
                    if(gn.y <= uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).y) {
                        if(gn.type == GameNote.NoteType.NORMAL || gn.type == GameNote.NoteType.ALT)
                            uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).visPress();
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }
                }
            }

        }
    }

    private void playAnim(GameNote note){
        if(note != null){
            String dirStr = note.dir.getDirectionAsString(Note.CapsMode.ALL_LOWERCASE); // wow that is one long line of code
            //System.out.println("Found note to dedlete");
            note.hit = true;
            switch(note.type){
                case ALT:
                case ALT_HOLD:
                    ago.playAnimation(dirStr + "-alt");
                    break;
                case NORMAL:
                case HOLD:
                    ago.playAnimation(dirStr);
                    break;
                default:
                    break;
                
            }
        }
    }
    private void buttonPress(int dir){
        if(keysPressed[dir]) return;
        uiNotes.get("Player").get(dir).visPress();
        //System.out.println("Im registering a press!");

        GameNote hitNote = findHittableNoteInLane(true, dir);
        playAnim(hitNote);

        removeNote(hitNote);
        keysPressed[dir] = true;
    }
    

    // very very barebones! this is all just for testing tho

    public Stage(){
        instance = this;
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

        ago = new AnimatedGameObject(200, 200, 1, cam);
        // TODO: make more moddable. make the frame data in some parsable text file so you can make reskins :D
        ago.addAnimationFromSpritesheet("left", 10, "./img/dude/left.png");
        ago.addAnimationFromSpritesheet("down", 10, "./img/dude/down.png");
        ago.addAnimationFromSpritesheet("up", 10, "./img/dude/up.png");
        ago.addAnimationFromSpritesheet("right", 10, "./img/dude/right.png");
        ago.addAnimationFromSpritesheet("left-alt", 8, "./img/dude/left-alt.png");
        ago.addAnimationFromSpritesheet("down-alt", 7, "./img/dude/down-alt.png");
        ago.addAnimationFromSpritesheet("up-alt", 8, "./img/dude/up-alt.png");
        ago.addAnimationFromSpritesheet("right-alt", 9, "./img/dude/right-alt.png");
        ago.addAnimationFromSpritesheet("idle", 11, "./img/dude/idle.png");
        cam.addObjectToLayer("Objects", ago);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0,false),
                "leftKeyPress");
            this.getActionMap().put("leftKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(0);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0,true),
                "leftKeyRelease");
            this.getActionMap().put("leftKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[0] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0,false),
                "downKeyPress");
            this.getActionMap().put("downKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(1);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0,true),
                "downKeyRelease");
            this.getActionMap().put("downKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[1] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0,false),
                "upKeyPress");
            this.getActionMap().put("upKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(2);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0,true),
                "upKeyRelease");
            this.getActionMap().put("upKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[2] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0,false),
                "rightKeyPress");
            this.getActionMap().put("rightKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(3);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0,true),
                "rightKeyRelease");
            this.getActionMap().put("rightKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[3] = false;
				}

            }
        );

        /*this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0,false),
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
        );*/

        String load = "mus/w1s1";
        try{
            load = Files.readString(Paths.get("./SONG_TO_LOAD_NAME.txt"));
        } catch(Exception e){
            e.printStackTrace();
        }

        // make it so hold notes render under normal notes
        cam.addRenderLayer("UI hold", cam.getLayerDepth("UI")-1);
        // make it so ui notes render under EVERY ui thing
        cam.addRenderLayer("UI note", cam.getLayerDepth("UI")-2);

        curChart = loadChart(load);
        updateThread.start();
    }
    
    private ArrayList<ArrayList<GameNote>> loadChart(String songName){
        // well here we are again. i will be doing the exact same implementation and changing very little
        // so basically im just porting 1:1
        ArrayList<ArrayList<GameNote>> ret = new ArrayList<>();
        curChartTypes = new ArrayList<>();
        Scanner scan = null;
        this.songName = songName;
        try{
            String fileName = "./charts/" + songName + ".swows";

            scan = new Scanner(new File(fileName));
            // skip the uuseless line
            scan.nextLine();

            bpm = scan.nextInt();
            System.out.println(bpm);

            scrollSpeed = scan.nextDouble();
            System.out.println(scrollSpeed);

            // skip another useless line
            scan.nextLine();

            File bleh = new File("./songs/" + songName +".wav");
            SoundManager.playSong(bleh.getAbsolutePath(), new Runnable() {
                @Override
                public void run() {
                    SoundManager.songClip.close();
                }
            });
            SoundManager.setVolume(1.7f);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bleh);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double durationInSeconds = (frames+0.0) / format.getFrameRate();

            songlong = (int) Math.round((durationInSeconds/60)*bpm*4);
            songbeat=((songlong/60*bpm*4)*(48*scrollSpeed));

            System.out.println(durationInSeconds + "\n" + songlong);

            // i havent even written anything and i already feel the urge to cry. god help me
            int b; // down.
            int bb; // across.

            int starty = 38;

            uiNotes.put("BadGuy", new HashMap<>());
            uiNotes.put("Player", new HashMap<>());

            for(bb = 0; bb < 8; bb++){
                ret.add(new ArrayList<>());
                curChartTypes.add(new ArrayList<>());
                int myx;
                if(bb<4){
                    myx=12+(60*bb);
                } else {
                    myx=234+50+(60*(bb-4));
                }
                // ui note
                UINote uin = new UINote(myx, starty, bb%4, cam, (songName.equals("mus_frostbytep2") && bb < 4), bb>=4);
                cam.addObjectToLayer("UI note", uin);
                uiNotes.get((bb < 4) ? "BadGuy" : "Player").put(bb%4, uin);

                // FUCKING NOTES BABYYYYY 
                for(b = 0; b < songlong; b++){
                    int line = scan.nextInt();
                    curChartTypes.get(bb).add(line);
                    //System.out.println(curChartTypes.get(bb).get(b));
                    if(line != 0){
                        GameNote gn = new GameNote(myx,(48+(b*48*scrollSpeed)), bb%4, cam, (songName.equals("mus_frostbytep2") && bb < 4), bb >= 4, curChartTypes.get(bb).get(b));
                        if(gn.isHold()) cam.addObjectToLayer("UI hold", gn);
                        else cam.addObjectToLayer("UI", gn);
                        ret.get(bb).add(gn);
                    }
                }
            }
            // wip
            /*
            GameNote gn = new GameNote(234+50+(60*3),(48+((songlong-1)*48*scrollSpeed)), 3, cam, false, true, GameNote.NoteType.END_SONG_TRIGGER);
            cam.addObjectToLayer("UI", gn);
            ret.get(7).add(gn);
            */

            scan.close();
            return ret;
        } catch(Exception e){
            // something went wrong with the chart loading!!!!
            e.printStackTrace();
            if(scan != null) scan.close();
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //cam.moveCamera(-1, -1);
        cam.drawViewport((Graphics2D) g);
        //cam.changeCameraZoom(-0.005);
        //cam.moveCamera(Math.cos(draws/5)*10, Math.sin(draws/5)*10);

        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        g.setColor(Color.white);
        g.drawString("misses: " + misses, 50, 50);
    }
}
