import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;
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
                "d");
            this.getActionMap().put("d",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					
				}

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0,false),
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
        );

        redrawTimer = new Timer(15, (e) -> {
            repaint();
            draws++;
        });

        redrawTimer.start();
        curChart = loadChart("mus_w3s1");
        updateThread.start();
    }
    
    private ArrayList<ArrayList<GameNote>> loadChart(String songName){
        // well here we are again. i will be doing the exact same implementation and changing very little
        // so basically im just porting 1:1
        ArrayList<ArrayList<GameNote>> ret = new ArrayList<>();
        curChartTypes = new ArrayList<>();
        Scanner scan = null;

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
                UINote uin = new UINote(myx, starty, bb%4, cam, false, bb>=4);
                cam.addObjectToLayer("UI", uin);

                // FUCKING NOTES BABYYYYY 
                for(b = 0; b < songlong; b++){
                    int line = scan.nextInt();
                    curChartTypes.get(bb).add(line);
                    //System.out.println(curChartTypes.get(bb).get(b));
                    if(line != 0){
                        GameNote gn = new GameNote(myx,(48+(b*48*scrollSpeed)), bb%4, cam, false, true, curChartTypes.get(bb).get(b));
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
