import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

public class TempOptionsMenu extends MusicBeatPanel implements KeyListener {
    int curSelected = 0;
    String[] options = {"downscroll: " + ClientPrefs.getDownscroll(), "change bindings", "", "back"};

    boolean binding = false;
    boolean ignoreNextBind = false;
    int bindStep = 0;

    public TempOptionsMenu(){super();}

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

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false),
            "enter");
        this.getActionMap().put("enter",
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(binding) return;
                    switch(curSelected){
                        case 0:
                            options[curSelected] = "downscroll: " + ClientPrefs.set("downscroll", !ClientPrefs.getDownscroll());
                            break;
                        case 1:
                            binding = true;
                            bindStep = 0;
                            ignoreNextBind = true;
                            addKeyListener(Main.tom);
                            break;
                        case 3:
                            FadeManager.fadeOut(Color.BLACK, 1, 1, ()->{
                                redraw.stop();
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

    private void changeSelection(int amt){
        curSelected += amt;
        if(curSelected < 0) curSelected = options.length-1;
        if(curSelected == options.length) curSelected = 0;
        if(options[curSelected].equals("")) changeSelection(amt);
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
        
        if(!binding){
            for(int i = 0; i < options.length; i++){
                if(curSelected == i) {
                    g.setColor(Color.YELLOW);
                    drawCenteredText("> "+options[i]+" <", 400+(50*i), g);
                } else {
                    g.setColor(Color.WHITE);
                    drawCenteredText(options[i], 400+(50*i), g);
                }
            }
        } else {
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
        }

        FadeManager.drawSelf((Graphics2D) g);
    }
}
