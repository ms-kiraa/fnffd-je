package panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import backend.Camera;
import backend.MusicBeatPanel;
import backend.managers.*;
import backend.save.ClientPrefs;
import effects.ColorReplaceEffect;
import effects.DudeSkins;
import main.Main;
import objects.fx.FXGameObject;

public class TempOptionsMenu extends MusicBeatPanel implements KeyListener {
    int curSelected = 0;
    String[] options = {"downscroll: " + ClientPrefs.getBoolean("downscroll"), "note debug: "+ClientPrefs.getBoolean("note_debug"), "change bindings", "change dude skin", "", "back"};

    boolean binding = false;
    boolean ignoreNextBind = false;
    int bindStep = 0;

    boolean choosingDudeSkin = false;
    int curDudeSkin = 0;
    DudeSkins[] skins;
    FXGameObject[] skinDisplays;

    Camera cam;

    // for dragging and dropping dude skins
    private Color dropConfirmColor = new Color(0, 150, 0);
    private Color dropErrorColor = new Color(150, 0, 0);
    private DropTarget target;
    private static BufferedImage template = null;
    private boolean dragged = false;
    private boolean showErrorMsg = false;

    public TempOptionsMenu(){super();}

    @Override
    protected void create(){
        cam = new Camera();
        skins = DudeSkins.values();
        skinDisplays = new FXGameObject[skins.length];
        // set up droptarget
        try{
            template = ImageIO.read(new File("./img/dude/skin-template.png"));
        } catch(Exception e){
            e.printStackTrace();
        }
        target = new DropTarget(this, new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                FadeManager.cancelFade();
                FadeManager.fadeOut(dropConfirmColor, 1, 1);
                dragged = true;
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {
                FadeManager.cancelFade();
                FadeManager.fadeIn(dropConfirmColor, 1, 1);
                dragged = false;
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
            // Accept copy drops
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            // Get the transfer which can provide the dropped item data
            Transferable transferable = dtde.getTransferable();
            // Get the data formats of the dropped item
            DataFlavor[] flavors = transferable.getTransferDataFlavors();

            // Loop through the flavors
            for (DataFlavor flavor : flavors) {
                try {
                    if (flavor.isFlavorJavaFileListType()) {
                        @SuppressWarnings("unchecked") // fuck you
                        java.util.List <File> files = (java.util.List<File>)  transferable.getTransferData(flavor);
                        for (File file : files) {
                            getColorsFromImage(file);
                        }
                    }
                } catch (Exception e) {
                    // Print out the error stack
                    e.printStackTrace();
                }
            }
            // Inform that the drop is complete
            dtde.dropComplete(true);
            if(!showErrorMsg) {
                FadeManager.cancelFade();
                FadeManager.fadeIn(dropConfirmColor, 1, 1);
            }
            dragged = false;
            }
            
        });
        for(int i = 0; i < skins.length; i++){
            if(skins[i] == DudeSkins.Custom && (ClientPrefs.customFromValues == null || ClientPrefs.customToValues == null)) continue;
            try {
                skinDisplays[i] = new FXGameObject(0, 0, 3, ImageIO.read(new File("./img/dude/skin-template.png")), cam);
                skinDisplays[i].setPosition(Main.windowWidth/2-(skinDisplays[i].image.getWidth()*skinDisplays[i].scale)/2, Main.windowHeight/2-(skinDisplays[i].image.getHeight()*skinDisplays[i].scale)/2);
                if(skins[i] != DudeSkins.Custom) {
                    skinDisplays[i].addEffect(skins[i].skin);
                } else {
                    skinDisplays[i].addEffect(new ColorReplaceEffect(ClientPrefs.customFromValues, ClientPrefs.customFromValues));
                }
                skinDisplays[i].visible = false;
                cam.addObjectToLayer("Objects", skinDisplays[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(!SoundManager.songClip.isOpen()) SoundManager.playSong("./snd/mus_game.wav", null);
        setBackground(Color.BLACK);
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0,false),
            "down");
        this.getActionMap().put("down",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(binding) return;
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
                    if(binding) return;
                    changeSelection(-1);
                }

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0,false),
            "left");
        this.getActionMap().put("left",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(binding || !choosingDudeSkin) return;
                    changeDudeSkinSelection(-1);
                }

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0,false),
            "right");
        this.getActionMap().put("right",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(binding || !choosingDudeSkin) return;
                    changeDudeSkinSelection(1);
                }

            }
        );

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
            "enter");
        this.getActionMap().put("enter",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(binding) return;
                    if(choosingDudeSkin) {
                        choosingDudeSkin = false;
                        ClientPrefs.dudeSkin = skins[curDudeSkin];
                        return;
                    }
                    switch(curSelected){
                        case 0:
                            options[curSelected] = "downscroll: " + ClientPrefs.set("downscroll", !ClientPrefs.getBoolean("downscroll"));
                            break;
                        case 1:
                            options[curSelected] = "note debug: " + ClientPrefs.set("note_debug", !ClientPrefs.getBoolean("note_debug"));
                            break;
                        case 2:
                            binding = true;
                            bindStep = 0;
                            ignoreNextBind = true;
                            addKeyListener(Main.tom);
                            break;
                        case 3:
                            for(int i = 0; i < DudeSkins.values().length; i++){
                                if(DudeSkins.values()[i] == ClientPrefs.dudeSkin) {
                                    curDudeSkin = i;
                                    break;
                                }
                            }
                            changeDudeSkinSelection(0);
                            choosingDudeSkin = true;
                            break;
                        case 5:
                            FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                                redraw.stop();
                                target.setActive(false);
                                Main.main.goToMainMenuPanel();
                            });
                            break;

                    }
                }

            }
        );
        FadeManager.fadeIn(Color.BLACK, 1, 1);
        super.create();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        boolean ignoring = ignoreNextBind;
        ignoreNextBind = false;
        if(ignoring) return;
        switch(bindStep){
            case 0:
                ClientPrefs.set("left_bind", e.getKeyCode());
                bindStep++;
                break;
            case 1:
                ClientPrefs.set("down_bind", e.getKeyCode());
                bindStep++;
                break;
            case 2:
                ClientPrefs.set("up_bind", e.getKeyCode());
                bindStep++;
                break;
            case 3:
                ClientPrefs.set("right_bind", e.getKeyCode());
                binding = false;
                removeKeyListener(this);
                break;
        }
    }

    private void getColorsFromImage(File img){
        BufferedImage bi = null;
        try{
            bi = ImageIO.read(img);
        }catch(Exception e){
            e.printStackTrace();
            return;
        }


        
        if(bi == null || bi.getWidth() != template.getWidth() || bi.getHeight() != template.getHeight()){
            showErrorMsg = true;
            FadeManager.fadeIn(dropErrorColor, 1, 1);
            Timer hide = new Timer(1000, (a)->{
                showErrorMsg = false;
            });
            hide.setRepeats(false);
            hide.start();
            return;
        }
        ArrayList<Color> changedColorsFrom = new ArrayList<>();
        ArrayList<Color> changedColorsTo = new ArrayList<>();
        for(int y = 0; y < bi.getHeight(); y++){
            for(int x = 0; x < bi.getWidth(); x++){
                Color pixel = new Color(bi.getRGB(x, y));
                //System.out.println(pixel);
                Color templatePixel = new Color(template.getRGB(x, y));
                if(!colorEquals(pixel, templatePixel) && !changedColorsFrom.contains(templatePixel)) {
                    System.out.println("FOUND COLOR MISMATCH AT "+x+", "+y+":\nWAS "+templatePixel+"\nIS NOW: "+pixel);
                    changedColorsFrom.add(templatePixel);
                    changedColorsTo.add(pixel);
                } else {
                    //System.out.println("looks like it checks out, template " + templatePixel + " response " + pixel);
                }
            }
        }
        ArrayList<ArrayList<Integer>> rgbFrom = new ArrayList<>();
        ArrayList<ArrayList<Integer>> rgbTo = new ArrayList<>();
        for(Color c : changedColorsFrom) {
            rgbFrom.add(new ArrayList<Integer>(Arrays.asList(c.getRed(), c.getGreen(), c.getBlue())));
        }
        for(Color c : changedColorsTo) {
            rgbTo.add(new ArrayList<Integer>(Arrays.asList(c.getRed(), c.getGreen(), c.getBlue())));
        }
        ClientPrefs.customFromValues = rgbFrom;
        ClientPrefs.customToValues = rgbTo;
        try {
            skinDisplays[skins.length-1] = new FXGameObject(0, 0, 3, ImageIO.read(new File("./img/dude/skin-template.png")), cam);
            skinDisplays[skins.length-1].setPosition(Main.windowWidth/2-(skinDisplays[skins.length-1].image.getWidth()*skinDisplays[skins.length-1].scale)/2, Main.windowHeight/2-(skinDisplays[skins.length-1].image.getHeight()*skinDisplays[skins.length-1].scale)/2);
            skinDisplays[skins.length-1].addEffect(new ColorReplaceEffect(rgbFrom, rgbTo));
            skinDisplays[skins.length-1].visible = false;
            cam.addObjectToLayer("Objects", skinDisplays[skins.length-1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean colorEquals(Color color1, Color color2){
        return (color1.getRGB() == color2.getRGB());
    }

    private void changeSelection(int amt){
        curSelected += amt;
        if(curSelected < 0) curSelected = options.length-1;
        if(curSelected == options.length) curSelected = 0;
        if(options[curSelected].equals("")) changeSelection(amt);
    }


    private void changeDudeSkinSelection(int amt){
        curDudeSkin += amt;
        if(curDudeSkin >= skinDisplays.length) curDudeSkin = 0;
        if(curDudeSkin < 0) curDudeSkin = skinDisplays.length-1;
        // im so fucking done man
        // HACK: THIS FUCKING SUCKS
        try{skinDisplays[curDudeSkin].visible = true;}catch(Exception e){
            curDudeSkin += amt;
            if(curDudeSkin >= skinDisplays.length) curDudeSkin = 0;
            if(curDudeSkin < 0) curDudeSkin = skinDisplays.length-1;
            changeDudeSkinSelection(0);
        }
        for(int i = 0; i < skinDisplays.length; i++){
            if(skinDisplays[i] == null) {
                continue;
            }
            if(skins[i] == DudeSkins.Custom && (ClientPrefs.customFromValues == null || ClientPrefs.customToValues == null)) continue;
            if(i != curDudeSkin) skinDisplays[i].visible = false;
        }
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
        drawCenteredText("welcome to the temporary options menu", 50, g);
        
        if(!binding && !choosingDudeSkin){
            for(int i = 0; i < options.length; i++){
                if(curSelected == i) {
                    g.setColor(Color.YELLOW);
                    drawCenteredText("> "+options[i]+" <", 400+(50*i), g);
                } else {
                    g.setColor(Color.WHITE);
                    drawCenteredText(options[i], 400+(50*i), g);
                }
            }
        } else if(binding) {
            String key = "";
            switch(bindStep){
                case 0:
                    key = "left";
                    break;
                case 1:
                    key = "down";
                    break;
                case 2:
                    key = "up";
                    break;
                case 3:
                    key = "right";
                    break;
            }
            drawCenteredText("binding " + key + " key", Main.windowHeight/2, g);
        } else if(choosingDudeSkin) {
            drawCenteredText("choose your dude.", 100, g);
            drawCenteredText("< " + skins[curDudeSkin].name + " >", Main.windowHeight-100, g);
            cam.drawViewport((Graphics2D) g);
        }

        FadeManager.drawSelf((Graphics2D) g);
        if(dragged){
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
            drawCenteredText("drop image to create custom dude skin!", Main.windowHeight/2, g);
        }
        if(showErrorMsg) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
            drawCenteredText("please drop a valid dude skin!", Main.windowHeight/2, g);
        }
    }
}
