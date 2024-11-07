package panels;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import backend.imagemanip.Bar;
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

    // song time calculation
    private double songDuration;
    private double songPosition;
    private double songProgress;
    private Bar timeBar = new Bar(Color.BLACK, Color.WHITE, 300, 25, 5, true, 0.5f, 5, 5).setFillMode(Bar.PUSH_FILL_IN);

    private Map<String, BufferedImage> ratingCache = new HashMap<>();  
    private ArrayList<Rating> activeRatings = new ArrayList<>();

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
    public boolean dudeLeftRightIdleStyle = false;

    public AnimatedGameObject badguy;
    public String badguyChar = "strad";
    public boolean badguyLeftRightIdleStyle = false;

    public FXAnimatedGameObject lady;
    public String ladyChar = "lady";
    public MultiImageGameObject speakers; // say hello to the first use of MultiImageGameObject since it was added however many months ago
    private int ladyLastBopDir = 0; // 0 = left 1 = right
    private boolean speakersChangeOnBeatHit;
    public boolean ladyLeftRightIdleStyle = true;

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

    private boolean paused = false;
    private BufferedImage pauseViewport = null;
    private String[] pausedOnAnims = {};
    private int[] pausedOnFrames = {};
    private long pauseTime = 0;

    public Map<String, FXGameObject> stageObjects = new HashMap<>();
    public Map<String, String> objectLayers = new HashMap<>(); // this is probably a bad way to do it

    // maybe i kill myself 🤔
    public Map<String, BufferedImage> stageImages = new HashMap<>();

    private Timer updateTimer;
    private Thread updateThread = new Thread(() -> {
        updateTimer = new Timer((int)(Main.TICK_TIME), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if(!loading && !paused) update();
                repaint();
                long elapsed = System.currentTimeMillis() - lastTickMillis; // probably will be used eventually
                currentFPS = 1000 / (int)elapsed;
                lastTickMillis = System.currentTimeMillis();
            }

        });
        updateTimer.start();
    });



    private int countdownBeat = 3;
    private BufferedImage countdownDisplay = null;
    private float countdownAlpha = 1;

    private GameNote findHittableNoteInLane(boolean playerLane, int lane){
        //System.out.println(lane + (playerLane ? 4 : 0));
        ArrayList<GameNote> laneList = curChart.get(lane + (playerLane ? 4 : 0));
        UINote ui = uiNotes.get(playerLane ? "Player" : "BadGuy").get(lane%4);
        int range = 140;

        for(GameNote note : laneList) {
            if(note.autohit || note.hit) continue;
            double ms = note.timeMS;
            double time = Conductor.getTime()*1000.0;
            // TODO: DOWNSCROLL IS BROKEN LMAOOOO IM GONNA SHOOT MYSELF WITH A SAWED OFF SHOTGUN
            if(ms >= time - range && ms <= time + range){
                double diff = Math.abs(ms-time);
                String rating = makeRating(diff);
                //System.out.println("visual distance (pixels): " + Math.abs(note.y-ui.y) + " | time difference (ms): " + diff + " | rating: " + rating);

                /*System.out.println(
                    "----------------FOUND A NOTE!----------------"+
                    "\n"+
                    "\ndist from strumbar: " + Math.abs(note.y-(ui.y+(!downscroll ? 0 : ui.image.getHeight()))) + 
                    "\n"+
                    "\nms: "+ms+
                    "\ntime: "+time+
                    "\n\ndifference of " + diff +" ms!!!");*/
                return note;
            }
        }
        return null;
    }

    private String makeRating(double diff) {
        Rating rating = null;
        String ratingName = null;
        if(diff <= 50) ratingName = "yeah";
        else if (diff <= 80) ratingName = "nice";
        else if (diff <= 110) ratingName = "eh";
        else ratingName = "crap";

        Main.main.panelSpecificDebugInfo.put(ratingName+"s", Integer.toString(Integer.parseInt(Main.main.panelSpecificDebugInfo.getOrDefault(ratingName+"s", "0"))+1));

        String filePath = "img/ui/ratings/" + ratingName + ".png";

        BufferedImage rateImg = ratingCache.get(ratingName);
        if(rateImg == null) {
            rateImg = FileRetriever.image(filePath);
            ratingCache.put(ratingName, rateImg);
        }
        rating = new Rating((speakers.x+speakers.image.getWidth()/2)-rateImg.getWidth()/2, speakers.y-rateImg.getHeight()/2, rateImg, cam);
        rating.scale = 0.5;
        rating.xvel = (Math.random()*2)-1;
        rating.yvel = (Math.random()*-3)-1;

        cam.addObjectToLayer("Objects", rating);
        activeRatings.add(rating);

        return ratingName;
    }

    // find what event to execute and do it
    private void doEvent(){
        //System.out.println("executing event " + event);
        //System.out.println("dats an event note right htere. " + event);
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

    private String formatToTime(double time) {
        int minutes = (int)Math.floor(time/60);
        String seconds = Integer.toString((int)Math.floor(time)%60);
        if(seconds.length() < 2) seconds = "0" + seconds;
        return minutes + ":" + seconds;
    }

    private void update(){
        luaInstance.fireLuaFunction("onUpdatePre");
        // move camera
        double camMoveSpd = 0.03;
        if(Math.abs(cam.x - camXTarget) > 5 || Math.abs(cam.y - camYTarget) > 5) cam.setCameraPos(lerp(cam.x, camXTarget, camMoveSpd), lerp(cam.y, camYTarget, camMoveSpd));
        songPosition = (SoundManager.songClip.getMicrosecondPosition() / 1000000.0);
        double dumbSongProgress = songPosition / songlong;
        Conductor.setTime(songPosition);
        double ymod = ((uiNotesYPos+dumbSongProgress * songbeat)*downscrollYMod);

        songProgress = songPosition/songDuration;
        timeBar.fillPercent = songProgress;

        //System.out.println(formatToTime(songDuration-songPosition));

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

        if(countdownBeat >= -1) return;

        // loop thru ratings and update (and remove) them
        ArrayList<Rating> ratingsToRemove = new ArrayList<>();
        for(Rating rating : activeRatings) {
            rating.update();
            if(!rating.active) {
                ratingsToRemove.add(rating);
                rating.visible = false;
                continue;
            }
        }
        for(Rating rating : ratingsToRemove) {
            activeRatings.remove(rating);
        }
        ratingsToRemove.clear();

        double curTimeMS = Conductor.getTime()*1000.0;
        //System.out.println(curTimeMS);
        ListIterator<ArrayList<GameNote>> iter1 = curChart.listIterator();
        while (iter1.hasNext()){
            ListIterator<GameNote> iter2 = iter1.next().listIterator();
            while(iter2.hasNext()){
                GameNote gn = iter2.next();
                gn.move(ymod);
                if(!(gn.y >= 0-gn.image.getHeight()*2 && gn.y <= 800+gn.image.getHeight())) continue;
                boolean missCondition = (downscroll && gn.y >= 500+gn.image.getHeight()) || (!downscroll && gn.y < 0-gn.image.getHeight());
                if(missCondition) {
                    removeNote(gn);
                    iter2.remove();
                    if(!gn.hit && !gn.autohit) {
                        //System.out.println("possible miss? how will the economy recover");
                        score -= 50;
                        misses++;
                    }
                    continue;
                }

                if(gn.autohit){
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

                    if(ClientPrefs.getBoolean("botplay")) {
                        if(gn.timeMS - curTimeMS < 5 && !gn.autohit) {
                            if(!gn.isHold()) findHittableNoteInLane(true, gn.dir.getDirectionAsInt());
                            playDudeAnim(gn);
                            removeNote(gn);
                            iter2.remove();
                            continue;
                        }
                    } else {
                        boolean holdHitCondition = Math.abs(curTimeMS-gn.timeMS) < 10;
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
        Main.main.panelSpecificDebugInfo.put("event", Integer.toString(event));
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

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
                "pause");
            this.getActionMap().put("pause",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    if(!paused) pause();
                    else unpause();
				}

            }
        );
    }

    private void pause(){
        paused = true;
        pauseTime = SoundManager.songClip.getMicrosecondPosition();
        SoundManager.songClip.stop();
        pauseViewport = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pauseG2 = pauseViewport.createGraphics();

        AnimatedGameObject[] chars = {badguy, dude};

        pausedOnAnims = new String[chars.length];
        pausedOnFrames = new int[chars.length];


        for(int i = 0; i < chars.length; i++) {
            AnimatedGameObject character = chars[i];
            pausedOnAnims[i] = character.curAnim;
            pausedOnFrames[i] = character.curFrame;
            System.out.println(character.curAnim + "\n" + character.curFrame);
            character.playAnimation("ayy");
        }

        cam.drawViewport(pauseG2);
        pauseG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        pauseG2.setColor(Color.BLACK);
        pauseG2.fillRect(0, 0, 800, 800);
        pauseG2.dispose();
    }

    private void unpause(){
        paused = false;
        SoundManager.songClip.start();
        SoundManager.songClip.setMicrosecondPosition(pauseTime);
        pauseViewport = null;

        AnimatedGameObject[] chars = {badguy, dude};
        for(int i = 0; i < chars.length; i++) {
            AnimatedGameObject character = chars[i];
            character.playAnimation(pausedOnAnims[i], pausedOnFrames[i]);
        }
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

            Thread countdown = new Thread(()->{
                songProgress = 0;
                while(countdownBeat >= -1) {
                    while(paused){}
                    switch (countdownBeat) {
                        case 0:
                            SoundManager.playSFX("snd/snd_go.wav");
                            countdownDisplay = FileRetriever.image("img/ui/go.png");
                            countdownAlpha = 1f;
                            break;
                        case -1:
                            SoundManager.playSong(songFile.getAbsolutePath(),null);
                        break;
                        default:
                            countdownDisplay = FileRetriever.image("img/ui/"+countdownBeat+".png");
                            countdownAlpha = 1f;
                            SoundManager.playSFX("snd/snd_" + countdownBeat + ".wav");
                            break;
                    }

                    if(countdownBeat != -1) {
                        if(ladyLeftRightIdleStyle) {
                            if(ladyLastBopDir == 0) {
                                lady.playAnimation("idle-right");
                            } else {
                                lady.playAnimation("idle-left");
                            }
                            ladyLastBopDir=(ladyLastBopDir+1)%2;
                        } else {
                            lady.playAnimation("idle");
                        }
                    }

                    countdownBeat--;
                    
                    try {
                        Thread.sleep(60000/(int)Conductor.bpm, (int)((60000.0/Conductor.bpm)%1)*10);
                    } catch (Exception e) {} // this will never ever happen
                }
            });

            countdown.start();
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
            songDuration = (frames+0.0) / format.getFrameRate();

            songlong = (int) Math.round((songDuration/60)*bpm*4);
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
                double ms = 0;
                ms -= (Conductor.getCrochetSec()/4)*1000.0;
                for(int b = 0; b < songlong; b++){
                    int line = scan.nextInt();
                    curChartTypes.get(bb).add(line);
                    //System.out.println(curChartTypes.get(bb).get(b));
                    double thisMS = ms;
                    ms += (Conductor.getCrochetSec()/4)*1000.0;
                    if(line != 0){
                        double y = (48+(b*48*scrollSpeed*dosc));
                        GameNote gn = new GameNote(myx,y, bb%4, cam, (songName.equals("mus_frostbytep2") && bb < 4), bb >= 4, curChartTypes.get(bb).get(b));
                        if(ClientPrefs.getBoolean("note_debug")) gn.drawHitbox = true;
                        gn.addEffect(colors.get(bb%4));
                        gn.timeMS = thisMS;
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
        Composite prev = g2.getComposite();
        AlphaComposite ac = null;

        if(!loading){
            //cam.moveCamera(-1, -1);
            BufferedImage vp = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
            cam.drawViewport(vp.createGraphics());
            //vp = ImageUtils.colorMultiply(vp, Color.GREEN);
            // ImageUtils.colorMultiply(vp, new Color(239, 0, 119))
            if(!paused) {
                g.drawImage(vp, 0, 0, null);
            } else {
                g.drawImage(pauseViewport, 0, 0, null);
                g.drawString("press q to quit", 400, 400);
            }
            for(String key : stageImages.keySet()) {
                BufferedImage bi = stageImages.get(key);
                try{
                    g.drawImage(bi, Main.windowWidth/2-bi.getWidth()/2, Main.windowHeight/2-bi.getHeight()/2, null);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            if(countdownAlpha > 0) {
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, countdownAlpha);
                g2.setComposite(ac);
                g2.drawImage(countdownDisplay, Main.windowWidth/2-countdownDisplay.getWidth(), Main.windowHeight/2-countdownDisplay.getHeight(), countdownDisplay.getWidth()*2, countdownDisplay.getHeight()*2, null);
                g2.setComposite(prev);
                countdownAlpha -= 0.05f;
            }

            if(FadeManager.fading) {
                FadeManager.drawSelf(g2);
            }
            //cam.changeCameraZoom(-0.005);
            //cam.moveCamera(Math.cos(draws/5)*10, Math.sin(draws/5)*10);

            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
            g.setColor(Color.white);
            g.drawString("fps: "+currentFPS, 50, 50);
            g.drawString("score: " + (ClientPrefs.getBoolean("botplay") ? "BOTPLAY" : score), 50, 70);
            g.drawString("misses: " + (ClientPrefs.getBoolean("botplay") ? "BOTPLAY" : misses), 50, 90);

            if(countdownBeat < -1) {
                int sin = (int)(Math.sin(Conductor.getStepDb()/2)*3);

                g2.drawImage(timeBar.makeBar(), Main.windowWidth/2-timeBar.width/2, 20+sin, null);

                g2.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
                FontMetrics fm = g2.getFontMetrics();
                String time = formatToTime(songDuration-songPosition);

                backend.TextUtils.drawOutlinedText(g2, time, Main.windowWidth/2-fm.stringWidth(time)/2, 41+sin, 2);
            }
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