import java.awt.Color;
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
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Stage extends JPanel {
    Camera cam;

    final int TICK_TIME = 15;

    Timer redrawTimer;
    int draws = 0;

    String songName;
    int bpm;
    double scrollSpeed;
    double songlong;
    double songbeat;
    double songpos;

    ArrayList<ArrayList<GameNote>> curChart;
    ArrayList<ArrayList<Integer>> curChartTypes;
    Map<String, Map<Integer, UINote>> uiNotes = new HashMap<>();

    public static Stage instance;

    boolean[] keysPressed = {false, false, false, false};

    Clip song;

    Timer updateTimer;
    Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(TICK_TIME*1.6), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }

        });
        updateTimer.start();
    });

    private GameNote findHittableNoteInLane(boolean playerLane, int lane){
        System.out.println(lane + (playerLane ? 4 : 0));
        ArrayList<GameNote> laneList = curChart.get(lane + (playerLane ? 4 : 0));
        UINote ui = uiNotes.get(playerLane ? "Player" : "BadGuy").get(lane%4);
        int range = 40;

        for(GameNote note : laneList) {
            if(note.y >= ui.y - range && note.y <= ui.y + range){
                return note;
            }
        }
        return null;
    }

    private void removeNote(GameNote gn){
        cam.removeObjectFromLayer("UI", gn);
        gn = null;
    }
    private void update(){
        songpos = (song.getMicrosecondPosition() / 1000000.0);
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
                    continue;
                }

                if(gn.type == GameNote.NoteType.EVENT){
                    //if(Math.round(Math.random()*5) == 2) Main.s = new Stage();
                    if(gn.y <= uiNotes.get("Player").get(3).y) {
                        System.out.println("dats an event note right htere.");
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }
                }
                if(gn.playerNote){
                    // handle player note stuff
                    if( 
                        (gn.type == GameNote.NoteType.HOLD || gn.type == GameNote.NoteType.ALT_HOLD) && 
                        keysPressed[gn.dir.getDirectionAsInt()] && 
                        gn.y <= uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y
                    ){
                        removeNote(gn);
                        iter2.remove();
                        continue;
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

    // prob temp
    public void playSound(String soundFile) {
        try{
            File f = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());  
            song = AudioSystem.getClip();
            song.open(audioIn);
            song.start();
            song.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP)
                        //if(paused || bar.score == 0) return;
                        song.close();
                        /*System.out.println(paused + "BEFORE PAUSEDH AF" + !paused);
                        // song should be over
                        // made too much stuff static... can't really do much except close the game

                        if(!Main.gamedOver && !paused) {
                            if(paused || bar.score == 0) return;
                            Game.loopExtraSound("snd/mus_game.wav");
                            FadeManager.fadeOut(Color.BLACK, 1,1,new Runnable() {

                            @Override
                            public void run() {
                                System.out.println(paused);
                                if(Main.gamedOver || paused) {
                                    System.out.println("UGHHHGHHSDGHUJWHGJUGJHEHRIJGE");
                                    return;
                                }
                                System.out.println("going ocne");
                                Main.main.toMenuFromGame();
                            }
                            });
                        }*/
                        
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
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

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0,false),
                "leftKeyPress");
            this.getActionMap().put("leftKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    if(keysPressed[0]) return;
					uiNotes.get("Player").get(0).visPress();

                    GameNote hitNote = findHittableNoteInLane(true, 0);
                    if(hitNote != null){
                        System.out.println("Found note to dedlete");
                        removeNote(hitNote);
                    }
                    keysPressed[0] = true;
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
                    if(keysPressed[1]) return;
					uiNotes.get("Player").get(1).visPress();

                    GameNote hitNote = findHittableNoteInLane(true, 1);
                    if(hitNote != null){
                        System.out.println("Found note to dedlete");
                        removeNote(hitNote);
                    }
                    keysPressed[1] = true;
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
                    if(keysPressed[2]) return;
					uiNotes.get("Player").get(2).visPress();

                    GameNote hitNote = findHittableNoteInLane(true, 2);
                    if(hitNote != null){
                        System.out.println("Found note to dedlete");
                        removeNote(hitNote);
                    }
                    keysPressed[2] = true;
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
                    if(keysPressed[3]) return;
					uiNotes.get("Player").get(3).visPress();

                    GameNote hitNote = findHittableNoteInLane(true, 3);
                    if(hitNote != null){
                        System.out.println("Found note to dedlete");
                        removeNote(hitNote);
                    }
                    keysPressed[3] = true;
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

        redrawTimer = new Timer(15, (e) -> {
            repaint();
            draws++;
        });

        redrawTimer.start();
        String load = "mus/w1s1";
        try{
            load = Files.readString(Paths.get("./SONG_TO_LOAD_NAME.txt"));
        } catch(Exception e){
            e.printStackTrace();
        }
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
            playSound(bleh.getAbsolutePath());

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
                cam.addObjectToLayer("UI", uin);
                uiNotes.get((bb < 4) ? "BadGuy" : "Player").put(bb%4, uin);

                // FUCKING NOTES BABYYYYY 
                for(b = 0; b < songlong; b++){
                    int line = scan.nextInt();
                    curChartTypes.get(bb).add(line);
                    //System.out.println(curChartTypes.get(bb).get(b));
                    if(line != 0){
                        GameNote gn = new GameNote(myx,(48+(b*48*scrollSpeed)), bb%4, cam, (songName.equals("mus_frostbytep2") && bb < 4), bb >= 4, curChartTypes.get(bb).get(b));
                        cam.addObjectToLayer("UI", gn);
                        ret.get(bb).add(gn);
                    }
                }
            }

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
    }
}
