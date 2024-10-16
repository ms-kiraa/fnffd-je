package panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import backend.MusicBeatPanel;
import backend.Conductor;
import backend.managers.*;
import backend.save.*;
import effects.ColorReplaceEffect;
import effects.DudeSkins;
import main.Main;
import objects.*;
import objects.fx.FXAnimatedGameObject;

public class TitlePanel extends MusicBeatPanel {
    BufferedImage pfp;
    double imageScale = 2;
    boolean skippedIntro = false;
    boolean doodledipped = false;

    String[] chosenFunny;
    
    // assets
    GameObject bg;
    FXAnimatedGameObject javaDude;
    GameObject enter;
    GameObject logo;

    public TitlePanel(){  
        super();
    }

    @Override
    protected void create(){
        ArrayList<String> funnies = getFunnies();
        setBackground(Color.CYAN);
        SoundManager.playSong("./snd/mus_game.wav", null);
        SoundManager.setSongLoop(true);
        Conductor.bpm = 115;
        
        chosenFunny = funnies.get((int)(Math.random()*funnies.size())).split("~");
        System.out.println(chosenFunny[0]);
        pfp = SplashPanel.pfp;

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
                "enter");
            this.getActionMap().put("enter",
            new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
                    if(!skippedIntro){
                        skipIntro(false);
                    } else {
                        if(doodledipped) return;
                        FadeManager.fadeIn(Color.WHITE, 1, 3);
                        doodledipped = true;
                        SoundManager.playSFX("./snd/snd_josh.wav");
                        Timer t = new Timer(1000, (a)->{
                            FadeManager.cancelFade();
                            //SoundManager.songClip.stop();
                            FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                                Main.main.goToMainMenuPanel();
                            });
                        });
                        t.setRepeats(false);
                        t.start();
                    }
				}

            }
        );

        super.create();

        try {
            BufferedImage bgI = ImageIO.read(new File("./img/ui/menu/main/spr_menubacksg_0.png"));
            bg = new GameObject(-150, -70, 2.7, bgI, cam);
            cam.addObjectToLayer("UI", bg);
            double logoSize = 4;
            BufferedImage logoI = ImageIO.read(new File("./img/ui/menu/main/spr_title_0.png"));
            double c1 = ((Main.windowWidth/2)-(logoI.getWidth()*logoSize)/2);
            System.out.println(logoI.getWidth());
            logo = new GameObject(c1, 50, logoSize, logoI, cam);
            logo.scrollFactor = 0;
            cam.addObjectToLayer("UI", logo);
            javaDude = new FXAnimatedGameObject(0, 0, 3, cam);
            if(ClientPrefs.dudeSkin == DudeSkins.Custom) {
                javaDude.addEffect(new ColorReplaceEffect(ClientPrefs.customFromValues, ClientPrefs.customToValues));
            } else {
                javaDude.addEffect(ClientPrefs.dudeSkin.skin);
            }
            javaDude.addAnimationFromSpritesheet("boing", 10, "./img/ui/menu/main/dumb.png");
            javaDude.frameTimeMS = 44;
            BufferedImage fr = javaDude.animations.get("boing").get(0);
            javaDude.setPosition((Main.windowWidth/2)-((fr.getWidth()*3)/2), Main.windowHeight-((((fr.getHeight()*3)/4)*3.6)));
            cam.addObjectToLayer("UI", javaDude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getFunnies(){
        ArrayList<String> funnies = new ArrayList<>();
        Scanner funnyScanner = null;
        try{
            funnyScanner = new Scanner(new File("./title_texts.txt"));
            while(funnyScanner.hasNextLine()){
                funnies.add(funnyScanner.nextLine());
            }
            funnyScanner.close();
        }catch(Exception e){
            e.printStackTrace();
            if(funnyScanner != null) funnyScanner.close();
        }
        return funnies;
    }
    int lineBufferHeight = 60;
    private void drawSillyText(Graphics g, String text){
        text = text.endsWith("\n") ? text + " " : text;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 48));
        FontMetrics fm = g.getFontMetrics();
        String[] lines = text.split("\n");
        int totalHeight = fm.getHeight() * lines.length + (lineBufferHeight*lines.length-1);
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            g.drawString(line, Main.windowWidth/2-fm.stringWidth(line)/2, getHeight()/2-(((totalHeight/2)/lines.length)*((lines.length-1)-i)));
        }
    }

    private void drawSillyText(Graphics g, String text, int xOff, int yOff){
        text = text.endsWith("\n") ? text + " " : text;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 48));
        FontMetrics fm = g.getFontMetrics();
        String[] lines = text.split("\n");
        int totalHeight = fm.getHeight() * lines.length + (lineBufferHeight*lines.length-1);
        for(int i = 0; i < lines.length; i++){
            String line = lines[i].toLowerCase();
            g.drawString(line, (Main.windowWidth/2-fm.stringWidth(line)/2)+xOff, (getHeight()/2-(((totalHeight/2)/lines.length)*((lines.length-1)-i)))+yOff);
        }
    }

    private void skipIntro(boolean natural){
        skippedIntro = true;
        if(!natural) SoundManager.songClip.setMicrosecondPosition((long)(8390*1000));
        FadeManager.fadeIn(Color.WHITE, 1, 5);
    }
    
    @Override
    protected void stepHit(int curStep){
        if(skippedIntro && curStep % 2 == 0){
            javaDude.playAnimation("boing");
        }
    }
    @Override
    protected void beatHit(int curBeat){
        super.beatHit(curBeat);
    }

    private void drawIntroText(Graphics g){
        if(!skippedIntro){
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Main.windowWidth, getHeight());
            int portTextOffset = 0;
            int bandaidFix = 70;
            switch(curBeat){
                case 0:
                case 1:
                    String silly = "free download by\n";
                    if(curBeat == 1) silly += "tyler_mon & funne";
                    drawSillyText(g, silly, 0, 0);
                    break;
                case 2:
                    drawSillyText(g, "ported to\n\n", 0, portTextOffset);
                    //if(!skippedIntro) skipIntro(false);
                    break;
                case 3:
                    if(curStep - (4*curBeat) <= 1){
                        drawSillyText(g, "ported to\njava by\n", 0, portTextOffset);
                    } else {
                        drawSillyText(g, "ported to\njava by\nkira", 0, portTextOffset);
                        if(pfp != null) {
                            g.drawImage(pfp, Main.windowWidth/2-((int)(pfp.getWidth()*imageScale)/2), getHeight()-(50+(int)(pfp.getHeight()*imageScale)), (int)(pfp.getWidth()*imageScale), (int)(pfp.getHeight()*imageScale), null);
                        }
                    }
                    break;
                case 4:
                case 5:
                    String funny = chosenFunny[0]+"\n";
                    if(curBeat == 5) funny += chosenFunny[1];
                    drawSillyText(g, funny);
                    break;
                case 6:
                    if(curStep-(4*curBeat) <= 1){
                        drawSillyText(g, "FNF\n\n\n", 0, bandaidFix);
                    } else {
                        drawSillyText(g, "FNF\nFREE\n\n", 0, bandaidFix);
                    }
                    break;
                case 7:
                    if(curStep-(4*curBeat) <= 1){
                        drawSillyText(g, "FNF\nFREE\nDOWNLOAD\n", 0, bandaidFix);
                    } else {
                        drawSillyText(g, "FNF\nFREE\nDOWNLOAD\nJAVA EDITION", 0, bandaidFix);
                    }
                    break;
                case 8:
                    skipIntro(true);
                    javaDude.playAnimation("boing");
                    break;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        if(skippedIntro){
            cam.drawViewport(g2);
        }

        FadeManager.drawSelf(g2);

        drawIntroText(g);
    }
}
