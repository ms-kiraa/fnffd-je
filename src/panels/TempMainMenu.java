package panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import backend.MusicBeatPanel;
import backend.data.SongData;
import backend.managers.*;
import main.Main;

public class TempMainMenu extends MusicBeatPanel {
    private int curSelected = 0;
    private String[] options = {"play", "freeplay menu", "open options", "cutscene editor"};
    private boolean selected = false;

    public TempMainMenu(){super();}

    @Override
    protected void create(){
        if(!SoundManager.songClip.isOpen()) SoundManager.playSong("./snd/mus_game.wav", null);
        setBackground(Color.BLACK);
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0,false),
            "down");
        this.getActionMap().put("down",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(selected) return;
                    changeSelection(1);
                }

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0,false),
            "up");
        this.getActionMap().put("up",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(selected) return;
                    changeSelection(-1);
                }

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
            "enter");
        this.getActionMap().put("enter",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(selected) return;
                    FadeManager.fadeIn(Color.WHITE, 1, 3);
                    selected = true;
                    SoundManager.playSFX("./snd/snd_josh.wav");
                    Timer t = new Timer(1000, (a)->{
                        FadeManager.cancelFade();
                        FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                            redraw.stop();
                            switch(options[curSelected]){
                                case "play":
                                    boolean done = false;
                                    String chosenSongs = "";
                                    while(!done){
                                        String song = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "what songs u want (type 'done' when finished)");
                                        if(!song.equals("done")){
                                            boolean found = false;
                                            for(File f : new File("./songs").listFiles()){
                                                if(f.getName().equals(song)){
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if(found){
                                                chosenSongs += song + "\n";
                                                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "added "+new SongData(song).formalName);
                                            } else {
                                                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "i dont think a song called "+song+" exists", "dumb fuck", JOptionPane.INFORMATION_MESSAGE);
                                            }
                                        } else if(chosenSongs.length() > 0) {
                                            done = true;
                                        } else {
                                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "you gotta add a song man", "dumb fuck", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                    try {
                                        Files.write(Paths.get("./SONG_TO_LOAD_NAME.txt"), Arrays.asList(chosenSongs));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    SoundManager.songClip.loop(0);
                                    SoundManager.songClip.stop();
                                    Main.main.goToStage();
                                    break;
                                case "freeplay menu":
                                    Main.main.goToFreeplay();
                                    break;
                                case "open options":
                                    Main.main.goToOptionsPanel();
                                    break;
                                case "cutscene editor":
                                    Main.main.goToCutsceneEditor();
                                    break;
                            }
                        });
                    });
                    t.setRepeats(false);
                    t.start();
                }

            }
        );
        FadeManager.fadeIn(Color.BLACK, 1, 1);
        super.create();
    }

    private void changeSelection(int amt){
        curSelected += amt;
        if(curSelected < 0) curSelected = options.length-1;
        if(curSelected == options.length) curSelected = 0;
    }

    private void drawCenteredText(String drawString, int y, Graphics g){
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(drawString);
        g.drawString(drawString, getWidth()/2-(width/2), y);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
        drawCenteredText("welcome to the temporary main menu", 50, g);
        
        for(int i = 0; i < options.length; i++){
            if(curSelected == i) {
                g.setColor(Color.YELLOW);
                drawCenteredText("> "+options[i]+" <", 400+(50*i), g);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredText(options[i], 400+(50*i), g);
            }
        }

        FadeManager.drawSelf((Graphics2D) g);
    }
}
