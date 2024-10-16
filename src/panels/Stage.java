package panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import backend.Camera;
import backend.Conductor;
import backend.FreeDownloadLua;
import backend.FileRetriever;
import backend.data.SongData;
import backend.managers.*;
import backend.save.ClientData;
import backend.save.ClientPrefs;
import effects.ColorReplaceEffect;
import effects.DudeSkins;
import main.Main;
import notes.*;
import objects.*;
import objects.fx.FXAnimatedGameObject;
import objects.fx.FXGameObject;

public class Stage extends JPanel {

    // loading
    private boolean loading = false;
    private String loadingStep = "null";
    private int curLoadStep = 0;
    private int maxLoadStep = 5;
    private boolean drawLoadingBar = true;

    public Camera cam;
    private int[] dudeCamMove = {230, -128};
    private int[] badguyCamMove = {115, -128};
    private int[] middleCamMove = {168, -128};
    public double camXTarget = 168;
    public double camYTarget = -128;

    public int event = 0;
    public int misses = 0;
    public int score = 0;

    public SongData songData;
    private File songFile;
    public String songName;
    public int bpm;
    private double scrollSpeed;
    private double songlong;
    private double songbeat;
    private double songpos;

    private long lastTickMillis = System.currentTimeMillis();

    public static ArrayList<String> songPlaylist = new ArrayList<>();

    public Map<String, Map<Integer, UINote>> uiNotes = new HashMap<>();
    public ArrayList<ArrayList<GameNote>> curChart;
    private ArrayList<ArrayList<Integer>> curChartTypes;
    

    public static Stage instance;

    private boolean[] keysPressed = {false, false, false, false};
    private int[] binds = ClientPrefs.getBinds();

    public FreeDownloadLua luaInstance;

    public FXAnimatedGameObject dude;
    public String dudeChar = "dude";
    private boolean dudeLeftRightIdleStyle = false;

    public AnimatedGameObject badguy;
    public String badguyChar = "strad";
    private boolean badguyLeftRightIdleStyle = false;

    public FXAnimatedGameObject lady;
    public String ladyChar = "lady";
    public MultiImageGameObject speakers; // say hello to the first use of MultiImageGameObject since it was added however many months ago
    private int ladyLastBopDir = 0; // 0 = left 1 = right
    private boolean speakersChangeOnBeatHit;
    private boolean ladyLeftRightIdleStyle = true;

    public int beat = -1;
    public int halfBeatInSteps = 0;
    public int step = 0;
    private int dudeLastIdleBeat = -10;
    private int dudeLastIdleDir = 0;
    private int badguyLastIdleBeat = -10;
    private int badguylastIdleDir = 0;

    public boolean downscroll = false;
    private int downscrollYMod = downscroll ? -1 : 1;
    public int uiNotesYPos = downscroll ? 480-38 : 38;

    public int currentFPS;

    public Map<String, FXGameObject> stageObjects = new HashMap<>();
    public Map<String, String> objectLayers = new HashMap<>(); // this is probably a bad way to do it

    // maybe i kill myself 🤔
    public Map<String, BufferedImage> stageImages = new HashMap<>();

    private Timer updateTimer;
    private Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(Main.TICK_TIME), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if(!loading) update();
                repaint();
                long elapsed = System.currentTimeMillis() - lastTickMillis; // probably will be used eventually
                currentFPS = 1000 / (int)elapsed;
                lastTickMillis = System.currentTimeMillis();
            }

        });
        updateTimer.start();
    });

    private GameNote findHittableNoteInLane(boolean playerLane, int lane){
        // TODO: fucking better input man this shit sucks ass, do ms based or SOMETHING just dont do y pos based please
        //System.out.println(lane + (playerLane ? 4 : 0));
        ArrayList<GameNote> laneList = curChart.get(lane + (playerLane ? 4 : 0));
        UINote ui = uiNotes.get(playerLane ? "Player" : "BadGuy").get(lane%4);
        int range = 40;

        for(GameNote note : laneList) {
            if((note.y+note.image.getHeight()/2) >= ui.y - range && note.y <= ui.y + range && !note.autohit && !note.hit){
                return note;
            }
        }
        return null;
    }

    // find what event to execute and do it
    private void doEvent(){
        //System.out.println("executing event " + event);
        
        luaInstance.fireLuaFunction("event", event);
        event++;
    }

    private void removeNote(GameNote gn){
        if(gn.isHold()) cam.removeObjectFromLayer("UI hold", gn);
        else cam.removeObjectFromLayer("UI", gn);
        gn = null;
    }

    double lerp(double a, double b, double f)
    {
        return a * (1.0 - f) + (b * f);
    }

    private void update(){
        //System.out.println("Oughh");
        /*if(keysPressed[0]) cam.moveCameraX(-1);
        if(keysPressed[1]) cam.moveCameraY(1);
        if(keysPressed[2]) cam.moveCameraY(-1);
        if(keysPressed[3]) cam.moveCameraX(1);
        System.out.println(cam.x + " " + cam.y);
        //*/

        luaInstance.fireLuaFunction("onUpdatePre");
        // move camera
        double camMoveSpd = 0.03;
        if(Math.abs(cam.x - camXTarget) > 5 || Math.abs(cam.y - camYTarget) > 5) cam.setCameraPos(lerp(cam.x, camXTarget, camMoveSpd), lerp(cam.y, camYTarget, camMoveSpd));
        songpos = (SoundManager.songClip.getMicrosecondPosition() / 1000000.0);
        double songProgress = songpos / songlong;
        Conductor.setTime(songpos);
        //System.out.println(Conductor.getCrochetSec());
        double ymod = ((uiNotesYPos+songProgress * songbeat)*downscrollYMod);

        //System.out.println(Conductor.getBeat());

        if(beat != Conductor.getBeat()){
            beat = Conductor.getBeat();
            luaInstance.fireLuaFunction("onBeatHit", beat);
            //System.out.println(Conductor.getStep());
            //System.out.println("oughhhh my beatssss");
            if(speakersChangeOnBeatHit) speakers.displayNextImage();
        }
        if(Conductor.getStep() != step) {
            step = Conductor.getStep();
            luaInstance.fireLuaFunction("onStepHit", step);
            if(step % 2 == 0) {
                if(ladyLeftRightIdleStyle){
                    if(ladyLastBopDir == 0) {
                        lady.playAnimation("idle-right");
                    } else {
                        lady.playAnimation("idle-left");
                    }
                    ladyLastBopDir = (ladyLastBopDir + 1) % 2;
                } else {
                    lady.playAnimation("idle");
                }
            }
            if(beat - badguyLastIdleBeat >= 2  && step % 4 == 0) {
                if(!badguyLeftRightIdleStyle){
                    badguy.playAnimation("idle");
                } else {
                    if(badguylastIdleDir == 0){
                        badguy.playAnimation("idle-right");
                    } else {
                        badguy.playAnimation("idle-left");
                    }
                    badguylastIdleDir = (badguylastIdleDir + 1) % 2;
                }
            }
            if(beat - dudeLastIdleBeat >= 2 && step % 4 == 0) {
                if(!dudeLeftRightIdleStyle){
                    dude.playAnimation("idle");
                } else {
                    if(dudeLastIdleDir == 0){
                        dude.playAnimation("idle-right");
                    } else {
                        dude.playAnimation("idle-left");
                    }
                    dudeLastIdleDir = (dudeLastIdleDir + 1) % 2;
                }
            }
        }
        ListIterator<ArrayList<GameNote>> iter1 = curChart.listIterator();
        while (iter1.hasNext()){
            ListIterator<GameNote> iter2 = iter1.next().listIterator();
            while(iter2.hasNext()){
                GameNote gn = iter2.next();
                gn.move(ymod);
                if(!(gn.y >= 0-gn.image.getHeight() && gn.y <= 800+gn.image.getHeight())) continue;
                boolean missCondition = (downscroll) ? (gn.y >= (500+gn.image.getHeight())) : (gn.y < 0-gn.image.getHeight());
                if(missCondition) {
                    removeNote(gn);
                    iter2.remove();
                    if(!gn.hit && !gn.autohit) {
                        System.out.println("possible miss? how will the economy recover");
                        score -= 50;
                        misses++;
                    }
                    continue;
                }

                if(gn.autohit){
                    //if(Math.round(Math.random()*5) == 2) Main.s = new Stage();
                    boolean autohitCondition = (downscroll) ? (gn.y >= uiNotesYPos) : (gn.y <= uiNotesYPos);
                    if(autohitCondition && !gn.hit) {
                        gn.hit = true;
                        switch(gn.type){
                            case AYY:
                                if(gn.playerNote){
                                    if(dude.hasAnimation("ayy")) {
                                        dude.playAnimation("ayy");
                                        dudeLastIdleBeat = beat;
                                    }
                                    File ayy = new File("./img/"+dudeChar+"/ayy.wav");
                                    if(ayy.exists()){
                                        SoundManager.playSFX(ayy.getAbsolutePath());
                                    }
                                } else {
                                    if(badguy.hasAnimation("ayy")) {
                                        badguy.playAnimation("ayy");
                                        badguyLastIdleBeat = beat;
                                    }
                                    File ayy = new File("./img/"+badguyChar+"/ayy.wav");
                                    if(ayy.exists()){
                                        SoundManager.playSFX(ayy.getAbsolutePath());
                                    }
                                }
                                break;
                            case END_SONG_TRIGGER:
                                System.out.println("EJNDING SONG!!");
                                // show loading screen
                                loading = true;
                                loadingStep = "FINISHING UP";
                                drawLoadingBar = false;
                                Thread endSongThread = new Thread(()->{
                                    SoundManager.songClip.close();
                                    updateTimer.stop();
                                    updateThread.interrupt();

                                    int[] prevScores = ClientData.getSongScore(songName);

                                    if(prevScores != null) {
                                        int[] overwrittenScores = {(prevScores[0] > score ? prevScores[0] : score), (prevScores[1] < misses ? prevScores[1] : misses)};
                                        ClientData.setSongScore(songName, overwrittenScores);
                                    } else {
                                        int[] scores = {score, misses};
                                        System.out.println(scores[0] + " " + scores[1]);
                                        ClientData.setSongScore(songName, scores);
                                    }

                                    // advance story playlist
                                    songPlaylist.remove(0);

                                    if(songPlaylist.size() != 0) Main.main.goToStage();
                                    else Main.main.goToMainMenuPanel();
                                });
                                endSongThread.setName("Song End Thread");
                                endSongThread.start();
                                break;
                            case EVENT:
                                System.out.println("dats an event note right htere. " + event);
                                doEvent();
                                break;
                            case DUDE_CAM:
                                camXTarget = dudeCamMove[0];
                                camYTarget = dudeCamMove[1];
                                break;
                            case ENEMY_CAM:
                                camXTarget = badguyCamMove[0];
                                camYTarget = badguyCamMove[1];
                                break;
                            case MIDDLE_CAM:
                                camXTarget = middleCamMove[0];
                                camYTarget = middleCamMove[1];
                                break;
                            default:
                                break;
                        }
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }
                }

                if(gn.playerNote){
                    // handle player note stuff
                    // this line is so big it might as well have its own variable
                    boolean downscrollHoldHitCondition = ((gn.y+gn.image.getHeight()) >= (uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y+uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).image.getHeight()));
                    boolean holdHitCondition = (downscroll) ? downscrollHoldHitCondition : (gn.y <= uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y);
                    if(
                        (gn.isHold()) &&
                        keysPressed[gn.dir.getDirectionAsInt()] &&
                        holdHitCondition &&
                        !gn.autohit
                    ){
                        playDudeAnim(gn);
                        removeNote(gn);
                        score += 25;
                        iter2.remove();
                        continue;
                    }

                    if(gn.autohit && gn.y <= uiNotes.get("Player").get(gn.dir.getDirectionAsInt()).y){

                    }
                } else {
                    boolean badguyDownscrollHoldHitCondition = ((gn.y+gn.image.getHeight()) >= (uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).y+uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).image.getHeight()));
                    boolean badguyHitCondition = (downscroll) ? badguyDownscrollHoldHitCondition : (gn.y <= uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).y);
                    if(badguyHitCondition) {
                        if(gn.type == GameNote.NoteType.NORMAL || gn.type == GameNote.NoteType.ALT)
                            uiNotes.get("BadGuy").get(gn.dir.getDirectionAsInt()).visPress();
                        playBadGuyAnim(gn);
                        removeNote(gn);
                        iter2.remove();
                        continue;
                    }
                }
            }

        }
        luaInstance.fireLuaFunction("onUpdate");
    }

    private void playDudeAnim(GameNote note){
        if(note != null){
            dudeLastIdleBeat = beat;
            dudeLastIdleDir = 1; // set next dir to left (aka resetting)
            String dirStr = note.dir.getDirectionAsString(Note.CapsMode.ALL_LOWERCASE);
            //System.out.println("Found note to dedlete");
            note.hit = true;
            switch(note.type){
                case ALT:
                case ALT_HOLD:
                    if(dude.hasAnimation(dirStr + "-alt")){
                        dude.playAnimation(dirStr + "-alt");
                    } else {
                        dude.playAnimation(dirStr);
                    }
                    break;
                case NORMAL:
                case HOLD:
                    dude.playAnimation(dirStr);
                    break;
                default:
                    break;

            }
        }
    }

    private void playBadGuyAnim(GameNote note){
        if(note != null){
            badguyLastIdleBeat = beat;
            badguylastIdleDir = 1; // set next dir to left (aka resetting)
            String dirStr = note.dir.getDirectionAsString(Note.CapsMode.ALL_LOWERCASE);
            //System.out.println("Found note to dedlete");
            note.hit = true;
            switch(note.type){
                case ALT:
                case ALT_HOLD:
                    if(badguy.hasAnimation(dirStr + "-alt")){
                        badguy.playAnimation(dirStr + "-alt");
                    } else {
                        badguy.playAnimation(dirStr);
                    }
                    break;
                case NORMAL:
                case HOLD:
                    badguy.playAnimation(dirStr);
                    break;
                default:
                    break;

            }
        }
    }

    private void playDudeMissAnim(int dir){
        //System.out.println("playing miss anim");
        dudeLastIdleBeat = beat;
        dudeLastIdleDir = 1;
        String dirStr = Note.Direction.getIntAsDirection(dir).getDirectionAsString(Note.CapsMode.ALL_LOWERCASE); // wow that is one long line of code
        if(dude.hasAnimation(dirStr + "-miss")) dude.playAnimation(dirStr + "-miss");
    }

    private void buttonPress(int dir){
        if(keysPressed[dir]) return;
        uiNotes.get("Player").get(dir).visPress();
        //System.out.println("Im registering a press!");

        GameNote hitNote = findHittableNoteInLane(true, dir);
        if(hitNote != null) {
            score += 100;
            playDudeAnim(hitNote);
            removeNote(hitNote);
        } else {
            playDudeMissAnim(dir);
            misses++;
            score -= 50;
            SoundManager.playSFX("./snd/snd_owch.wav", 1.15f);
        }
        keysPressed[dir] = true;
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private Map<String, Object> getAnimationData(String charName){
        File animData = new File("./img/" + charName + "/animData.txt");
        Map<String, Object> ret = new HashMap<>();
        ret.put("has-arrow-poses", true);
        ret.put("has-alts", false);
        ret.put("has-misses", false);
        ret.put("has-ayy", false);
        ret.put("left-right-idle", false);
        ret.put("scale", 1.0);

        if(animData.exists()){
            Scanner dataScanner = null;
            try{
                dataScanner = new Scanner(animData);
                while(dataScanner.hasNextLine()){
                    String next = dataScanner.nextLine();
                    String[] colonSplit = next.split(":");
                    ret.put(colonSplit[0], (isDouble(colonSplit[1]) ? Double.parseDouble(colonSplit[1]) : Boolean.parseBoolean(colonSplit[1])));
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
            Scanner frameScanner = null;
            int ret = 1;
            try{
                frameScanner = new Scanner(frameData);
                while(frameScanner.hasNextLine()){
                    String next = frameScanner.next();
                    String sub = next.substring(0, Math.min(anim.length(), next.length()));
                    if(sub.equals(anim)){
                        ret = Integer.parseInt(next.split(":")[1]);
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

    private void loadDudeCharacter(String charName){
        // first things first, get all the info
        Map<String, Object> data = getAnimationData(charName);
        boolean hasPoses = (boolean) data.get("has-arrow-poses");
        boolean hasAlts = (boolean) data.get("has-alts");
        boolean hasMisses = (boolean) data.get("has-misses");
        boolean hasAyy = (boolean) data.get("has-ayy");
        boolean leftRightIdle = (boolean) data.get("left-right-idle");
        double scale = (double) data.get("scale");
        dude = new FXAnimatedGameObject(525, 290, scale, cam);
        if(ClientPrefs.dudeSkin != DudeSkins.Default) {
            if(ClientPrefs.dudeSkin == DudeSkins.Custom){
                dude.addEffect(new ColorReplaceEffect(ClientPrefs.customFromValues, ClientPrefs.customToValues));
            } else {
                dude.addEffect(ClientPrefs.dudeSkin.skin);
            }
        }

        if(hasPoses){
            dude.addAnimationFromSpritesheet("left", getFrameData("left", charName), "./img/"+charName+"/left.png");
            dude.addAnimationFromSpritesheet("down", getFrameData("down", charName), "./img/"+charName+"/down.png");
            dude.addAnimationFromSpritesheet("up", getFrameData("up", charName), "./img/"+charName+"/up.png");
            dude.addAnimationFromSpritesheet("right", getFrameData("right", charName), "./img/"+charName+"/right.png");
        }
        if(hasMisses){
            dude.addAnimationFromSpritesheet("left-miss", getFrameData("left-miss", charName), "./img/"+charName+"/left-miss.png");
            dude.addAnimationFromSpritesheet("down-miss", getFrameData("down-miss", charName), "./img/"+charName+"/down-miss.png");
            dude.addAnimationFromSpritesheet("up-miss", getFrameData("up-miss", charName), "./img/"+charName+"/up-miss.png");
            dude.addAnimationFromSpritesheet("right-miss", getFrameData("right-miss", charName), "./img/"+charName+"/right-miss.png");
        }
        if(hasAlts){
            dude.addAnimationFromSpritesheet("left-alt", getFrameData("left-alt", charName), "./img/"+charName+"/left-alt.png");
            dude.addAnimationFromSpritesheet("down-alt", getFrameData("down-alt", charName), "./img/"+charName+"/down-alt.png");
            dude.addAnimationFromSpritesheet("up-alt", getFrameData("up-alt", charName), "./img/"+charName+"/up-alt.png");
            dude.addAnimationFromSpritesheet("right-alt", getFrameData("right-alt", charName), "./img/"+charName+"/right-alt.png");
        }
        if(hasAyy) dude.addAnimationFromSpritesheet("ayy", getFrameData("ayy", charName), "./img/"+charName+"/ayy.png");
        if(!leftRightIdle){
            dude.addAnimationFromSpritesheet("idle", getFrameData("idle", charName), "./img/"+charName+"/idle.png");
        } else {
            dude.addAnimationFromSpritesheet("idle-left", getFrameData("idle-left", charName), "./img/"+charName+"/idle-left.png");
            dude.addAnimationFromSpritesheet("idle-right", getFrameData("idle-right", charName), "./img/"+charName+"/idle-right.png");
        }
        dudeLeftRightIdleStyle = leftRightIdle;
        cam.addObjectToLayer("Objects", dude);

        File offsets = new File("./img/"+charName+"/offsets.txt");
        if(offsets.exists()){
            Scanner offsetScan = null;
            try{
                offsetScan = new Scanner(offsets);
                while(offsetScan.hasNext()){
                    String line = offsetScan.nextLine();
                    String[] colonSplit = line.split(":");
                    dude.addOffset(colonSplit[0], Integer.parseInt(colonSplit[1].split(",")[0]), Integer.parseInt(colonSplit[1].split(",")[1]));
                }
                offsetScan.close();
            } catch (Exception e){
                e.printStackTrace();
                if(offsetScan != null) offsetScan.close();
            }
        }
        String idle = "idle";
        if(!dude.animations.containsKey("idle")) idle = "idle-left";
        dude.setPosition(dude.x, dude.y - dude.animations.get(idle).get(0).getHeight()*scale);
    }

    // this is DUMB!
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private Map<String, Object> getLadyConfig(String lady){
        Map<String, Object> ret = new HashMap<>();
        ret.put("draw-speakers", true);
        ret.put("draw-lady", true);
        ret.put("speakers-change-on-beat-hit", true);
        ret.put("lady-x-offset", 0);
        ret.put("lady-y-offset", 0);
        File configFile = new File("./img/"+lady+"/ladyConfig.txt");
        if(configFile.exists()){
            Scanner configScanner = null;
            try{
                configScanner = new Scanner(configFile);
                while(configScanner.hasNextLine()){
                    String line = configScanner.nextLine();
                    String[] split = line.split(":");
                    if(isInteger(split[1])){
                        ret.put(split[0], Integer.parseInt(split[1]));
                    } else {
                        // its hopefully just bool
                        ret.put(split[0], Boolean.parseBoolean(split[1]));
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                if(configScanner != null) configScanner.close();
            }
        }
        return ret;
    }

    private void loadLadyCharacter(String charName){
        // first things first, get all the info
        Map<String, Object> data = getAnimationData(charName);
        boolean hasPoses = (boolean) data.get("has-arrow-poses");
        boolean hasAlts = (boolean) data.get("has-alts");
        boolean hasMisses = (boolean) data.get("has-misses");
        boolean hasAyy = (boolean) data.get("has-ayy");
        boolean leftRightIdle = (boolean) data.get("left-right-idle");
        double scale = (double) data.get("scale");

        Map<String, Object> config = getLadyConfig(charName);
        boolean drawSpeakers = (boolean) config.get("draw-speakers");
        boolean drawLady = (boolean) config.get("draw-lady");
        speakersChangeOnBeatHit = (boolean) config.get("speakers-change-on-beat-hit");
        int ladyXOffset = (int) config.get("lady-x-offset");
        int ladyYOffset = (int) config.get("lady-y-offset");

        lady = new FXAnimatedGameObject(400, 250, scale, cam);
        if(ClientPrefs.dudeSkin == DudeSkins.Kira) {
            // hi mika
            lady.addEffect(new ColorReplaceEffect(
                Arrays.asList(
                    Arrays.asList(198,192,179),
                    Arrays.asList(88,61,95),
                    Arrays.asList(63,114,112),
                    Arrays.asList(187,201,208),
                    Arrays.asList(53,69,77)
                ),
                Arrays.asList(
                    Arrays.asList(88, 68, 48), // hair
                    Arrays.asList(245, 47, 106), // streak
                    Arrays.asList(224, 83, 109), // dress
                    Arrays.asList(209, 119, 150), // socks
                    Arrays.asList(110, 48, 89) // shoes
                )));
        }
        if(hasPoses){
            lady.addAnimationFromSpritesheet("left", getFrameData("left", charName), "./img/"+charName+"/left.png");
            lady.addAnimationFromSpritesheet("down", getFrameData("down", charName), "./img/"+charName+"/down.png");
            lady.addAnimationFromSpritesheet("up", getFrameData("up", charName), "./img/"+charName+"/up.png");
            lady.addAnimationFromSpritesheet("right", getFrameData("right", charName), "./img/"+charName+"/right.png");
        }
        if(hasMisses){
            lady.addAnimationFromSpritesheet("left-miss", getFrameData("left-miss", charName), "./img/"+charName+"/left-miss.png");
            lady.addAnimationFromSpritesheet("down-miss", getFrameData("down-miss", charName), "./img/"+charName+"/down-miss.png");
            lady.addAnimationFromSpritesheet("up-miss", getFrameData("up-miss", charName), "./img/"+charName+"/up-miss.png");
            lady.addAnimationFromSpritesheet("right-miss", getFrameData("right-miss", charName), "./img/"+charName+"/right-miss.png");
        }
        if(hasAlts){
            lady.addAnimationFromSpritesheet("left-alt", getFrameData("left-alt", charName), "./img/"+charName+"/left-alt.png");
            lady.addAnimationFromSpritesheet("down-alt", getFrameData("down-alt", charName), "./img/"+charName+"/down-alt.png");
            lady.addAnimationFromSpritesheet("up-alt", getFrameData("up-alt", charName), "./img/"+charName+"/up-alt.png");
            lady.addAnimationFromSpritesheet("right-alt", getFrameData("right-alt", charName), "./img/"+charName+"/right-alt.png");
        }
        if(hasAyy) lady.addAnimationFromSpritesheet("ayy", getFrameData("ayy", charName), "./img/"+charName+"/ayy.png");
        if(!leftRightIdle){
            lady.addAnimationFromSpritesheet("idle", getFrameData("idle", charName), "./img/"+charName+"/idle.png");
        } else {
            lady.addAnimationFromSpritesheet("idle-right", getFrameData("idle-right", charName), "./img/"+charName+"/idle-right.png");
            lady.addAnimationFromSpritesheet("idle-left", getFrameData("idle-left", charName), "./img/"+charName+"/idle-left.png");
        }
        ladyLeftRightIdleStyle = leftRightIdle;
        if(drawLady) cam.addObjectToLayer("Objects", lady);

        File offsets = new File("./img/"+charName+"/offsets.txt");
        if(offsets.exists()){
            Scanner offsetScan = null;
            try{
                offsetScan = new Scanner(offsets);
                while(offsetScan.hasNext()){
                    String line = offsetScan.nextLine();
                    String[] colonSplit = line.split(":");
                    lady.addOffset(colonSplit[0], Integer.parseInt(colonSplit[1].split(",")[0]), Integer.parseInt(colonSplit[1].split(",")[1]));
                }
                offsetScan.close();
            } catch (Exception e){
                e.printStackTrace();
                if(offsetScan != null) offsetScan.close();
            }
        }
        lady.setPosition(lady.x, lady.y - lady.animations.get(leftRightIdle ? "idle-left" : "idle").get(0).getHeight()*scale);
        ArrayList<BufferedImage> speakerFrames = new ArrayList<>();
        File speakersFolder = new File("./img/"+charName+"/speakers");
        if(speakersFolder.exists()){
            File[] frames = speakersFolder.listFiles();
            for(File frame : frames){
                if(frame.isFile()){
                    speakerFrames.add(FileRetriever.image(frame.getAbsolutePath()));
                }
            }
        }
        BufferedImage dumbFrame = lady.animations.get(leftRightIdle ? "idle-left" : "idle").get(0);
        speakers = new MultiImageGameObject(lady.x+dumbFrame.getWidth()/2, lady.y+dumbFrame.getHeight()*scale, 1, cam, speakerFrames, 0);
        if(drawSpeakers) cam.addObjectToLayer("Speakers", speakers);
        speakers.setPosition(speakers.x-speakerFrames.get(0).getWidth()/2, speakers.y - (speakerFrames.get(0).getHeight()/3)*2);
        for(String key : lady.animations.keySet()){
            lady.addOffset(key, lady.offsets.get(key)[0] + ladyXOffset, lady.offsets.get(key)[1] + ladyYOffset);
        }
    }

    private void loadBadGuyCharacter(String charName){
        // first things first, get all the info
        Map<String, Object> data = getAnimationData(charName);
        boolean hasPoses = (boolean) data.get("has-arrow-poses");
        boolean hasAlts = (boolean) data.get("has-alts");
        boolean hasMisses = (boolean) data.get("has-misses");
        boolean hasAyy = (boolean) data.get("has-ayy");
        boolean leftRightIdle = (boolean) data.get("left-right-idle");
        double scale = (double) data.get("scale");

        badguy = new AnimatedGameObject(260, 290, scale, cam);
        if(hasPoses){
            badguy.addAnimationFromSpritesheet("left", getFrameData("left", charName), "./img/"+charName+"/left.png");
            badguy.addAnimationFromSpritesheet("down", getFrameData("down", charName), "./img/"+charName+"/down.png");
            badguy.addAnimationFromSpritesheet("up", getFrameData("up", charName), "./img/"+charName+"/up.png");
            badguy.addAnimationFromSpritesheet("right", getFrameData("right", charName), "./img/"+charName+"/right.png");
        }
        if(hasMisses){
            badguy.addAnimationFromSpritesheet("left-miss", getFrameData("left-miss", charName), "./img/"+charName+"/left-miss.png");
            badguy.addAnimationFromSpritesheet("down-miss", getFrameData("down-miss", charName), "./img/"+charName+"/down-miss.png");
            badguy.addAnimationFromSpritesheet("up-miss", getFrameData("up-miss", charName), "./img/"+charName+"/up-miss.png");
            badguy.addAnimationFromSpritesheet("right-miss", getFrameData("right-miss", charName), "./img/"+charName+"/right-miss.png");
        }
        if(hasAlts){
            badguy.addAnimationFromSpritesheet("left-alt", getFrameData("left-alt", charName), "./img/"+charName+"/left-alt.png");
            badguy.addAnimationFromSpritesheet("down-alt", getFrameData("down-alt", charName), "./img/"+charName+"/down-alt.png");
            badguy.addAnimationFromSpritesheet("up-alt", getFrameData("up-alt", charName), "./img/"+charName+"/up-alt.png");
            badguy.addAnimationFromSpritesheet("right-alt", getFrameData("right-alt", charName), "./img/"+charName+"/right-alt.png");
        }
        if(hasAyy) badguy.addAnimationFromSpritesheet("ayy", getFrameData("ayy", charName), "./img/"+charName+"/ayy.png");
        if(!leftRightIdle){
            badguy.addAnimationFromSpritesheet("idle", getFrameData("idle", charName), "./img/"+charName+"/idle.png");
        } else {
            badguy.addAnimationFromSpritesheet("idle-right", getFrameData("idle-right", charName), "./img/"+charName+"/idle-right.png");
            badguy.addAnimationFromSpritesheet("idle-left", getFrameData("idle-left", charName), "./img/"+charName+"/idle-left.png");
        }
        badguyLeftRightIdleStyle = leftRightIdle;
        cam.addObjectToLayer("Objects", badguy);

        File offsets = new File("./img/"+charName+"/offsets.txt");
        if(offsets.exists()){
            Scanner offsetScan = null;
            try{
                offsetScan = new Scanner(offsets);
                while(offsetScan.hasNext()){
                    String line = offsetScan.nextLine();
                    String[] colonSplit = line.split(":");
                    String[] commaSplit = colonSplit[1].split(",");
                    badguy.addOffset(colonSplit[0], Integer.parseInt(commaSplit[0]), Integer.parseInt(commaSplit[1]));
                }
                offsetScan.close();
            } catch (Exception e){
                e.printStackTrace();
                if(offsetScan != null) offsetScan.close();
            }
        }
        // get dem feet on the ground!
        String idle = "idle";
        if(!badguy.animations.containsKey("idle")) idle = "idle-left";
        dude.setPosition(badguy.x, badguy.y - (badguy.animations.get(idle).get(0).getHeight()*scale));
    }

    private void setBinds(){
        this.getInputMap().put(KeyStroke.getKeyStroke(binds[0], 0,false),
                "leftKeyPress");
            this.getActionMap().put("leftKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(0);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[0], 0,true),
                "leftKeyRelease");
            this.getActionMap().put("leftKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[0] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[1], 0,false),
                "downKeyPress");
            this.getActionMap().put("downKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(1);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[1], 0,true),
                "downKeyRelease");
            this.getActionMap().put("downKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[1] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[2], 0,false),
                "upKeyPress");
            this.getActionMap().put("upKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(2);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[2], 0,true),
                "upKeyRelease");
            this.getActionMap().put("upKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[2] = false;
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[3], 0,false),
                "rightKeyPress");
            this.getActionMap().put("rightKeyPress",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    buttonPress(3);
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(binds[3], 0,true),
                "rightKeyRelease");
            this.getActionMap().put("rightKeyRelease",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    keysPressed[3] = false;
				}

            }
        );
    }

    // very very barebones! this is all just for testing tho

    public Stage(){
        instance = this;
        FadeManager.cancelFade();
        cam = new Camera(0, 0, 1.5);
        cam.setCameraPos(168, -128);
        setBinds();

        downscroll = ClientPrefs.getBoolean("downscroll");
        downscrollYMod = downscroll ? -1 : 1;
        uiNotesYPos = downscroll ? 480-38 : 38;


        loading = true;
        curLoadStep++;
        loadingStep = "READING SONG FILE";
        updateThread.setName("Update Stage Thread");
        updateThread.start();
        Thread songLoadThread = new Thread(()->{
            String load = "mus_w1s1";
            Scanner songScanner = null;
            try{
                if(songPlaylist.size() == 0) {
                    songScanner = new Scanner(new File("./SONG_TO_LOAD_NAME.txt"));

                    while (songScanner.hasNextLine()){
                        String song = songScanner.nextLine();
                        if(song.length() > 0) {
                            songPlaylist.add(song);
                            System.out.println(song);
                        }
                    }

                    songScanner.close();
                }
                load = songPlaylist.get(0);
            } catch(Exception e){
                e.printStackTrace();
                if(songScanner != null) songScanner.close();
            }

            curLoadStep++;
            loadingStep = "INITIALIZING LUA SCRIPTS";
            songData = new SongData(load);
            luaInstance = new FreeDownloadLua(songData);

            luaInstance.fireLuaFunction("onCreatePre");

            curLoadStep++;
            loadingStep = "LOADING CHART";
            // make it so hold notes render under normal notes
            cam.addRenderLayer("UI hold", cam.getLayerDepth("UI")-1);
            // make it so ui notes render under EVERY ui thing
            cam.addRenderLayer("UI note", cam.getLayerDepth("UI")-2);
            curChart = loadChart(load);

            curLoadStep++;
            loadingStep = "PARSING SONG DATA";
            //Map<String, String> songData = parseSongData("./songs/"+load+"/data.txt");
            
            curLoadStep++;
            loadingStep = "LOADING CHARACTERS";
            dudeChar = songData.dudeChar; //songData.containsKey("dude-char") ? songData.get("dude-char") : "dude";
            loadDudeCharacter(dudeChar);
            badguyChar = songData.badguyChar; //songData.containsKey("badguy-char") ? songData.get("badguy-char") : "strad";
            loadBadGuyCharacter(badguyChar);
            cam.addRenderLayer("Speakers", cam.getLayerDepth("Background")+1);
            ladyChar = songData.ladyChar; //songData.containsKey("lady-char") ? songData.get("lady-char") : "lady";
            loadLadyCharacter(ladyChar);

            //curLoadStep++;
            //loadingStep = "LOADING STAGE";
            //String stage = songData.stage; //songData.containsKey("stage") ? songData.get("stage") : "backyard";
            //loadStageFile("./stages/"+stage);

            loading = false;
            luaInstance.fireLuaFunction("onCreate");
            
            // i really don't know why. i really, truly don't. but this increases the framerate by like 50% and it doesn't interfere with anything else so why not
            FadeManager.fadeOut(new Color(255, 255, 255 ,255), 1, Integer.MAX_VALUE);

            SoundManager.playSong(songFile.getAbsolutePath(),null);
        });
        songLoadThread.setName("Song Load Thread");
        songLoadThread.start();
    }

    private ArrayList<ArrayList<GameNote>> loadChart(String songName){
        // well here we are again. i will be doing the exact same implementation and changing very little
        // so basically im just porting 1:1
        ArrayList<ArrayList<GameNote>> ret = new ArrayList<>();
        curChartTypes = new ArrayList<>();
        Scanner scan = null;
        this.songName = songName;
        try{
            String fileName = "./songs/" + songName + "/" + songName + ".swows";

            scan = new Scanner(new File(fileName));
            // skip the uuseless line
            scan.nextLine();

            bpm = scan.nextInt();
            //System.out.println(bpm);
            Conductor.bpm = bpm;

            scrollSpeed = scan.nextDouble();
            //System.out.println(scrollSpeed);

            // skip another useless line
            //scan.nextLine();

            songFile = new File("./songs/" + songName + "/" + songName +".wav");
            // why tf did i make it a runnable????
            //SoundManager.setVolume(1.7f);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(songFile);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double durationInSeconds = (frames+0.0) / format.getFrameRate();

            songlong = (int) Math.round((durationInSeconds/60)*bpm*4);
            songbeat=((songlong/60*bpm*4)*(48*scrollSpeed));

            //System.out.println(durationInSeconds + "\n" + songlong);

            // upscroll = 38
            // downscroll = 480-38
            // whered i get 480? idk
            int starty = uiNotesYPos;

            int dosc = (starty == 38) ? 1 : -1;
            // BOOKMARK

            uiNotes.put("BadGuy", new HashMap<>());
            uiNotes.put("Player", new HashMap<>());

            ArrayList<ColorReplaceEffect> colors = new ArrayList<>();
            if(ClientPrefs.noteColors != null) {
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
            for(int bb = 0; bb < 8; bb++){
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
                for(int b = 0; b < songlong; b++){
                    int line = scan.nextInt();
                    curChartTypes.get(bb).add(line);
                    //System.out.println(curChartTypes.get(bb).get(b));
                    if(line != 0){
                        GameNote gn = new GameNote(myx,(48+(b*48*scrollSpeed*dosc)), bb%4, cam, (songName.equals("mus_frostbytep2") && bb < 4), bb >= 4, curChartTypes.get(bb).get(b));
                        if(ClientPrefs.getBoolean("note_debug")) gn.drawHitbox = true;
                        gn.addEffect(colors.get(bb%4));
                        if(gn.isHold()) cam.addObjectToLayer("UI hold", gn);
                        else cam.addObjectToLayer("UI", gn);
                        ret.get(bb).addLast(gn);
                    } else {
                        // if this is the first note in the column, skip it
                        if(ret.get(bb).size() < 1) continue;
                        // if last note was a hold or alt hold, set it to draw the note cap
                        GameNote.NoteType type = GameNote.NoteType.convertIntToNoteType(curChartTypes.get(bb).get(b-1));
                        if(type == GameNote.NoteType.HOLD ||
                           type == GameNote.NoteType.ALT_HOLD) {
                            GameNote gn = ret.get(bb).get(ret.get(bb).size()-1);
                            gn.enableCap();
                        }
                    }
                }
            }
            // wip
            GameNote gn = new GameNote(234+50+(60*3),(48+((songlong-1)*48*scrollSpeed*dosc)), 3, cam, false, true, GameNote.NoteType.END_SONG_TRIGGER);
            cam.addObjectToLayer("UI", gn);
            ret.get(7).add(gn);

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
        Graphics2D g2 = (Graphics2D) g;

        if(!loading){
            //cam.moveCamera(-1, -1);
            BufferedImage vp = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
            cam.drawViewport(vp.createGraphics());
            //vp = ImageUtils.colorMultiply(vp, Color.GREEN);
            // ImageUtils.colorMultiply(vp, new Color(239, 0, 119))
            g.drawImage(vp, 0, 0, null);
            for(String key : stageImages.keySet()) {
                BufferedImage bi = stageImages.get(key);
                try{
                    g.drawImage(bi, Main.windowWidth/2-bi.getWidth()/2, Main.windowHeight/2-bi.getHeight()/2, null);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            if(FadeManager.fading) {
                FadeManager.drawSelf(g2);
            }
            //cam.changeCameraZoom(-0.005);
            //cam.moveCamera(Math.cos(draws/5)*10, Math.sin(draws/5)*10);

            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
            g.setColor(Color.white);
            g.drawString("fps: "+currentFPS, 50, 50);
            g.drawString("score: " + score, 50, 70);
            g.drawString("misses: " + misses, 50, 90);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.white);
            String loadTitle = "LOADING!!";
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
            FontMetrics fm = g.getFontMetrics();
            int p = fm.stringWidth(loadTitle)/2;
            int h = fm.getHeight();
            g.drawString(loadTitle, (Main.windowWidth/2)-p, (Main.windowHeight/2)-h);

            g.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
            fm = g.getFontMetrics();
            p = fm.stringWidth(loadingStep)/2;
            h = fm.getHeight();
            g.drawString(loadingStep, (Main.windowWidth/2)-p, ((Main.windowHeight/2)+100)-h);

            if(drawLoadingBar){
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(Main.windowWidth/2-250, Main.windowHeight/2+200, 250*2, 40);
                g2.fillRect(Main.windowWidth/2-245, Main.windowHeight/2+205, (int)(((245*2)+1)*(curLoadStep*1.0/(maxLoadStep))), 31);
            }
        }
        g2.dispose();
    }
}