package main;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import backend.FrontUI;
import backend.MusicBeatPanel;
import backend.managers.FadeManager;
import backend.save.*;
import objects.snapblocks.SnapBlockTest;
import panels.*;
import panels.editors.*;

public class Main extends JFrame {

    public final static int windowWidth = 815-14;
    public final static int windowHeight = 833-7;

    public static Main main;

    public boolean debugUIEnabled = false;

    // this is probably a bad idea but meh
    public static RecordScratchScreen rss;
    public static TempMainMenu tmm;
    public static TempOptionsMenu tom;
    public static Stage s;
    public static TitlePanel tp;
    public static FreeplayPanel fm;

    public static final int targetFPS = 9999;
    public static final int TICK_TIME = 17;

    public Map<String, String> panelSpecificDebugInfo = new HashMap<>();

    public Main(String title){
        super(title);
        String load = "false";
        try{
            load = Files.readString(Paths.get("./LOAD_OFFSET_EDITOR.txt"));
        } catch(Exception e){
            e.printStackTrace();
        }
        boolean offset = Boolean.parseBoolean(load);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientPrefs.saveAllSets();
                ClientData.saveAllScores();
                setVisible(false);
                dispose();
                System.exit(0);
            }
        });
        getGlassPane().setVisible(true);
        setPreferredSize(new Dimension(windowWidth+14, windowHeight+7));
        setLayout(new BorderLayout());
        setResizable(false);

        // initialize settings
        ClientPrefs.init();
        // initialize scores
        ClientData.loadScores();

        setGlassPane(new FrontUI());
        
        if(!offset){
            getContentPane().add(new SplashPanel(), BorderLayout.CENTER);
        } else {
            getContentPane().add(new AnimationOffsetPanel(), BorderLayout.CENTER);
        }
        //this.getContentPane().setSize(windowWidth, windowHeight);
        pack();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // panel switching methods

    public void goToStage(){
        nullPanel(fm);
        s = new Stage();
        getContentPane().removeAll();
        getContentPane().add(s, BorderLayout.CENTER);
        revalidate();
        s.requestFocusInWindow();
        postSwitch(s);
    }

    public void goToRecordScratch(){
        rss = new RecordScratchScreen();
        getContentPane().removeAll();
        getContentPane().add(rss, BorderLayout.CENTER);
        revalidate();
        rss.requestFocusInWindow();
        postSwitch(rss);
    }

    public void goToTitlePanel(){
        tp = new TitlePanel();
        getContentPane().removeAll();
        getContentPane().add(tp, BorderLayout.CENTER);
        revalidate();
        tp.requestFocusInWindow();
        postSwitch(tp);
    }


    public void goToMainMenuPanel(){
        tmm = new TempMainMenu();
        getContentPane().removeAll();
        getContentPane().add(tmm, BorderLayout.CENTER);
        revalidate();
        tmm.requestFocusInWindow();
        postSwitch(tmm);
        if(s != null) s=null;
        nullPanel(tp);
        nullPanel(tom);
    }

    public void goToOptionsPanel(){
        tom = new TempOptionsMenu();
        getContentPane().removeAll();
        getContentPane().add(tom, BorderLayout.CENTER);
        revalidate();
        tom.requestFocusInWindow();
        postSwitch(tom);
        nullPanel(tmm);
        nullPanel(tp);
    }

    public void goToFreeplay(){
        fm = new FreeplayPanel();
        getContentPane().removeAll();
        getContentPane().add(fm, BorderLayout.CENTER);
        revalidate();
        fm.requestFocusInWindow();
        postSwitch(fm);
        nullPanel(tmm);
        nullPanel(tp);
    }

    public void goToCutsceneEditor(){
        CutsceneEditorPanel cep = new CutsceneEditorPanel();
        getContentPane().removeAll();
        getContentPane().add(cep, BorderLayout.CENTER);
        revalidate();
        cep.requestFocusInWindow();
        FadeManager.cancelFade();
        postSwitch(cep);
        nullPanel(tmm);
        nullPanel(tp);
    }



    // does stuff to a panel after it is added
    private void postSwitch(JPanel p) {
        p.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "DEBUG_TOGGLE");
        p.getActionMap().put("DEBUG_TOGGLE", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("hi");
                debugUIEnabled = !debugUIEnabled;
            }
            
        });
        System.out.println("added bnd!!!");
        panelSpecificDebugInfo.clear();
    }

    // does stuff to make a panel stop running
    private void nullPanel(MusicBeatPanel p) {
        if(p != null) {
            p.redraw.stop();
            p = null;
        }
    }
}
