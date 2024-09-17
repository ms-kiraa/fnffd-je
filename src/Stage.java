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
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Stage extends JPanel {
    // loading
    boolean loading = false;
    String loadingStep = "null";
    int curLoadStep = 0;
    int maxLoadStep = 5;
    boolean drawLoadingBar = true;

    Camera cam;
    int[] dudeCamMove = {230, -128};
    int[] badguyCamMove = {115, -128};
    int[] middleCamMove = {168, -128};
    double camXTarget = 168;
    double camYTarget = -128;

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

    public static ArrayList<String> songPlaylist = new ArrayList<>();

    ArrayList<ArrayList<GameNote>> curChart;
    ArrayList<ArrayList<Integer>> curChartTypes;
    Map<String, Map<Integer, UINote>> uiNotes = new HashMap<>();

    public static Stage instance;

    boolean[] keysPressed = {false, false, false, false};
    int[] binds = ClientPrefs.getBinds();

    Clip song;
    FreeDownloadLua luaInstance;

    AnimatedGameObject dude;
    String dudeChar = "dude";
    boolean dudeLeftRightIdleStyle = false;

    AnimatedGameObject badguy;
    String badguyChar = "strad";
    boolean badguyLeftRightIdleStyle = false;

    AnimatedGameObject lady;
    String ladyChar = "lady";
    MultiImageGameObject speakers; // say hello to the first use of MultiImageGameObject since it was added however many months ago
    int ladyLastBopDir = 0; // 0 = left 1 = right
    boolean speakersChangeOnBeatHit;
    boolean ladyLeftRightIdleStyle = true;

    int beat = -1;
    int halfBeatInSteps = 0;
    int step = 0;
    int dudeLastIdleBeat = -10;
    int dudeLastIdleDir = 0;
    int badguyLastIdleBeat = -10;
    int badguylastIdleDir = 0;

    boolean downscroll = false;
    int downscrollYMod = downscroll ? -1 : 1;
    int uiNotesYPos = downscroll ? 480-38 : 38;

    int currentFPS;
    ArrayList<Integer> recentFPS = new ArrayList<>();
    int roundedFPS;

    Map<String, GameObject> stageObjects = new HashMap<>();

    Timer updateTimer;
    Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(Main.TICK_TIME), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if(!loading) update();
                repaint();
                draws++;
                long elapsed = System.currentTimeMillis() - lastTickMillis; // probably will be used eventually
                currentFPS = 1000 / (int)elapsed;
                recentFPS.add(currentFPS);
                if(recentFPS.size() > 10) {
                    recentFPS.remove(0);
                }
                roundedFPS = 0;
                for(int i : recentFPS) {
                    roundedFPS += i;
                }
                roundedFPS /= recentFPS.size();
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
        luaInstance.updateGlobalValue("event", event);
        luaInstance.fireLuaFunction("event");
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
                boolean missCondition = (downscroll) ? (gn.y > getHeight()+gn.image.getHeight()) : (gn.y < 0-gn.image.getHeight());
                if(missCondition) {
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
                                new Thread(()->{
                                    SoundManager.songClip.close();
                                    updateTimer.stop();
                                    updateThread.interrupt();

                                    // advance story playlist
                                    songPlaylist.remove(0);

                                    if(songPlaylist.size() != 0) Main.main.goToStage();
                                    else Main.main.goToMainMenuPanel();
                                }).start();
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
            playDudeAnim(hitNote);
            removeNote(hitNote);
        } else {
            playDudeMissAnim(dir);
            misses++;
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
        dude = new AnimatedGameObject(525, 290, scale, cam);
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

        lady = new AnimatedGameObject(400, 250, scale, cam);
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
                    try{
                        speakerFrames.add(ImageIO.read(frame));
                    } catch(Exception e){
                        e.printStackTrace();
                    }
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

    private void loadStageFile(String path){
        String stagePath = path + "/";
        File stage = new File(stagePath + "stage.txt");
        System.out.println(stage.getAbsolutePath());
        if(stage.exists()){
            ArrayList<String> instructions = new ArrayList<>();
            Scanner stageScanner = null;
            try{
                stageScanner = new Scanner(stage);
                while (stageScanner.hasNextLine()) {
                    String next = stageScanner.nextLine();
                    instructions.add(next);
                }
                stageScanner.close();
            } catch(Exception e){
                e.printStackTrace();
                if(stageScanner != null) stageScanner.close();
            }
            String findQuoteTextRegex = "\"(.*?)\""; // allow for either 'this quote' or "this quote"
            Pattern quotePattern = Pattern.compile(findQuoteTextRegex); // find text in quotes, will be used for image and name
            Pattern twoNumbersCommaSeparatedPattern = Pattern.compile("(-?[0-9]+),\\s*(-?[0-9]+)"); // hee hee pp
            Pattern decimalPattern = Pattern.compile("([0-9]+\\.?[0-9]*)"); // looks for decimal nums
            Pattern threeNumbersCommaSeparatedPattern = Pattern.compile("([0-9]),\\s*([0-9]),\\s*([0-9])");
            
            int i = 0;
            for(String instruction : instructions) {
                i++;
                /*
                 * current command list
                 * make sprite
                 * set sprite scroll factor
                 * set dude x and y
                 * set badguy x and y
                 * change background of camera (sky thing)
                 */
                // HACK: this is probably a shitty way to do this, this uses substring SEVENTEEN FUCKING TIMES, find places to replace it with split() or something
                if(instruction.toLowerCase().startsWith("make sprite")){ // make sprite
                    // get the FUCKING parameters
                    Matcher m = quotePattern.matcher(instruction);
                    m.find();
                    String img = m.group(1);
                    //System.out.println(img);
                    BufferedImage bi = null;
                    try{
                        bi = ImageIO.read(new File(stagePath + img));
                    }catch(Exception e){
                        e.printStackTrace();
                        System.out.println(stagePath + img);
                        continue;
                    }
                    // get x and y
                    int x, y = 0;
                    Matcher mm = twoNumbersCommaSeparatedPattern.matcher(instruction);
                    mm.find();
                    x = Integer.parseInt(mm.group(1));
                    y = Integer.parseInt(mm.group(2));
                    System.out.println(x + " " + y);
                    // finally get name
                    m.find();
                    String name = m.group(1);
                    //System.out.println(name);
                    // make dat object
                    GameObject made = new GameObject(x, y, 1, bi, cam);
                    cam.addObjectToLayer("Background", made);
                    stageObjects.put(name, made);
                } else if(instruction.toLowerCase().startsWith("set scroll factor")){ // set scroll factor
                    //System.out.println("gotta set dat scroll!");
                    Matcher m = quotePattern.matcher(instruction);
                    m.find();
                    String name = m.group(1);

                    Matcher mm = decimalPattern.matcher(instruction);
                    mm.find(instruction.lastIndexOf("\""));
                    double newFactor = Double.parseDouble(mm.group(1));
                    System.out.println(newFactor);
                    stageObjects.get(name).scrollFactor = newFactor;
                } else if(instruction.toLowerCase().startsWith("set camera background")){
                    // TODO: add other support
                    String type = instruction.toLowerCase().substring(instruction.indexOf("TO ")+3, instruction.indexOf("\"")-1);
                    switch(type){
                        case "hex": // hex
                            // only supported thing atm lul!
                            Matcher m = quotePattern.matcher(instruction);
                            m.find();
                            String hex = m.group(1);
                            cam.setBackground(Color.decode("#" + hex.toLowerCase()));
                            break;
                        case "rgb":
                            Matcher mm = quotePattern.matcher(instruction);
                            mm.find();
                            String rgb = mm.group(1);
                            // now grab the little thingies
                            Matcher mmm = threeNumbersCommaSeparatedPattern.matcher(rgb);
                            mmm.find();
                            String r = mmm.group(1);
                            String g = mmm.group(2);
                            String b = mmm.group(3);
                            cam.setBackground(new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b)));
                        default:
                            JOptionPane.showMessageDialog(null, "Unknown color type: "+type);
                    }
                } else if(instruction.toLowerCase().startsWith("set dude")){ // set a property of dude
                    System.out.println(Character.toString(instruction.charAt(9)));
                    setPropertyOfAnimatedObject(instruction, dude);
                } else if(instruction.toLowerCase().startsWith("set badguy")){ // set a property of badguy
                    setPropertyOfAnimatedObject(instruction, badguy);
                } else if(instruction.toLowerCase().startsWith("set lady")) {
                    setPropertyOfAnimatedObject(instruction, lady);
                } else {
                    if(instruction.startsWith("#")) System.out.println("thats probably a comment");
                    else if(instruction.trim().length() == 0) System.out.println("just a blanlk line");
                    else {
                        JOptionPane.showMessageDialog(null, "Unknown command on line "+i+": "+instruction);
                    }
                }
            }
        }
    }

    private void setPropertyOfAnimatedObject(String instruction, AnimatedGameObject obj){
        String param = instruction.substring(instruction.indexOf("TO")-2);
        if(param.startsWith("X")){
            double x = Double.parseDouble(instruction.substring(instruction.indexOf("TO ")+3));
            obj.setPosition(x, obj.y);
        } else if(param.startsWith("Y")){
            double y = Double.parseDouble(instruction.substring(instruction.indexOf("TO ")+3));
            String idle = "idle";
            // if it doesnt have the normal "idle" animation, assume it's in left-right style
            if(!obj.animations.containsKey("idle")) idle = "idle-left";
            obj.setPosition(obj.x, y - obj.animations.get(idle).get(0).getHeight()* ((double)getAnimationData((obj.equals(badguy) ? badguyChar : dudeChar)).get("scale")));
        } else {
            System.out.println("???");
        }
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
    }

    private Map<String, String> parseSongData(String path){
        Map<String, String> ret = new HashMap<>();
        Scanner dataScanner = null;
        File dataFile = new File(path);
        if(dataFile.exists()){
            try{
                dataScanner = new Scanner(dataFile);
                while(dataScanner.hasNextLine()){
                    String next = dataScanner.nextLine();
                    String[] colonSplit = next.split(":");
                    String key = colonSplit[0];
                    String val = colonSplit[1];
                    ret.put(key, val);
                }
            }catch(Exception e){
                e.printStackTrace();
                if(dataScanner != null) dataScanner.close();
            }
        }
        return ret;
    }

    // very very barebones! this is all just for testing tho

    public Stage(){
        instance = this;
        FadeManager.cancelFade();
        cam = new Camera(0, 0, 1.5);
        cam.setCameraPos(168, -128);
        setBinds();

        downscroll = ClientPrefs.getDownscroll();
        downscrollYMod = downscroll ? -1 : 1;
        uiNotesYPos = downscroll ? 480-38 : 38;


        loading = true;
        curLoadStep++;
        loadingStep = "READING SONG FILE";
        updateThread.start();
        new Thread(()->{
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

        luaInstance = new FreeDownloadLua(new SongData(load));

        curLoadStep++;
        loadingStep = "PARSING SONG DATA";
        Map<String, String> songData = parseSongData("./songs/"+load+"/data.txt");

        curLoadStep++;
        loadingStep = "LOADING CHARACTERS";
        dudeChar = songData.containsKey("dude-char") ? songData.get("dude-char") : "dude";
        loadDudeCharacter(dudeChar);
        badguyChar = songData.containsKey("badguy-char") ? songData.get("badguy-char") : "strad";
        loadBadGuyCharacter(badguyChar);
        cam.addRenderLayer("Speakers", cam.getLayerDepth("Background")+1);
        ladyChar = songData.containsKey("lady-char") ? songData.get("lady-char") : "lady";
        loadLadyCharacter(ladyChar);

        curLoadStep++;
        loadingStep = "LOADING STAGE";
        String stage = songData.containsKey("stage") ? songData.get("stage") : "backyard";
        loadStageFile("./stages/"+stage);

        // make it so hold notes render under normal notes
        cam.addRenderLayer("UI hold", cam.getLayerDepth("UI")-1);
        // make it so ui notes render under EVERY ui thing
        cam.addRenderLayer("UI note", cam.getLayerDepth("UI")-2);

        curLoadStep++;
        loadingStep = "LOADING CHART";
        curChart = loadChart(load);

        luaInstance.fireLuaFunction("onCreate");
        loading = false;
        }).start();
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
            System.out.println(bpm);
            Conductor.bpm = bpm;

            scrollSpeed = scan.nextDouble();
            System.out.println(scrollSpeed);

            // skip another useless line
            //scan.nextLine();

            File bleh = new File("./songs/" + songName + "/" + songName +".wav");
            // why tf did i make it a runnable?????
            SoundManager.playSong(bleh.getAbsolutePath(),null);
            SoundManager.setVolume(1.7f);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bleh);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double durationInSeconds = (frames+0.0) / format.getFrameRate();

            songlong = (int) Math.round((durationInSeconds/60)*bpm*4);
            songbeat=((songlong/60*bpm*4)*(48*scrollSpeed));

            System.out.println(durationInSeconds + "\n" + songlong);

            // upscroll = 38
            // downscroll = 480-38
            // whered i get 480? idk
            int starty = uiNotesYPos;

            int dosc = (starty == 38) ? 1 : -1;
            // BOOKMARK

            uiNotes.put("BadGuy", new HashMap<>());
            uiNotes.put("Player", new HashMap<>());

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
            GameNote gn = new GameNote(234+50+(60*3),(48+((songlong-1)*48*scrollSpeed)), 3, cam, false, true, GameNote.NoteType.END_SONG_TRIGGER);
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

        if(!loading){
            //cam.moveCamera(-1, -1);
            cam.drawViewport((Graphics2D) g);
            FadeManager.drawSelf((Graphics2D) g);
            //cam.changeCameraZoom(-0.005);
            //cam.moveCamera(Math.cos(draws/5)*10, Math.sin(draws/5)*10);

            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
            g.setColor(Color.white);
            g.drawString("misses: " + misses, 50, 50);
            g.drawString("fps: "+currentFPS, 150, 50);
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
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(Main.windowWidth/2-250, Main.windowHeight/2+200, 250*2, 40);
                g2.fillRect(Main.windowWidth/2-245, Main.windowHeight/2+205, (int)(((245*2)+1)*(curLoadStep*1.0/(maxLoadStep))), 31);
                g2.dispose();
            }
        }
    }
}