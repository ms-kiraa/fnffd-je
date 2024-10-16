package main;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

import backend.save.*;
import panels.*;
import panels.editors.*;

public class Main extends JFrame {

    public final static int windowWidth = 815;
    public final static int windowHeight = 833;

    public static Main main;

    // this is probably a bad idea but meh
    public static RecordScratchScreen rss;
    public static TempMainMenu tmm;
    public static TempOptionsMenu tom;
    public static Stage s;
    public static TitlePanel tp;
    public static FreeplayPanel fm;

    public static final int targetFPS = 9999;
    public static final int TICK_TIME = 17;

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
                ClientData.saveScores();
                setVisible(false);
                dispose();
                System.exit(0);
            }
        });

        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(new BorderLayout());
        setResizable(false);

        // initialize settings
        ClientPrefs.init();
        // initialize scores
        ClientData.loadScores();

        if(!offset){
            getContentPane().add(new CutsceneEditorPanel(), BorderLayout.CENTER);
        } else {
            getContentPane().add(new AnimationOffsetPanel(), BorderLayout.CENTER);
        }

        pack();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // panel switching methods

    public void goToStage(){
        s = new Stage();
        getContentPane().removeAll();
        getContentPane().add(s, BorderLayout.CENTER);
        revalidate();
        s.requestFocusInWindow();
    }

    public void goToRecordScratch(){
        rss = new RecordScratchScreen();
        getContentPane().removeAll();
        getContentPane().add(rss, BorderLayout.CENTER);
        revalidate();
        rss.requestFocusInWindow();
    }

    public void goToTitlePanel(){
        tp = new TitlePanel();
        getContentPane().removeAll();
        getContentPane().add(tp, BorderLayout.CENTER);
        revalidate();
        tp.requestFocusInWindow();
    }


    public void goToMainMenuPanel(){
        if(s != null) s=null;
        if(tp != null) {
            tp.redraw.stop();
            tp=null;
        }
        if(tom!=null) tom=null;
        tmm = new TempMainMenu();
        getContentPane().removeAll();
        getContentPane().add(tmm, BorderLayout.CENTER);
        revalidate();
        tmm.requestFocusInWindow();
    }

    public void goToOptionsPanel(){
        if(tmm != null) tmm=null;
        if(tp != null) tp=null;
        tom = new TempOptionsMenu();
        getContentPane().removeAll();
        getContentPane().add(tom, BorderLayout.CENTER);
        revalidate();
        tom.requestFocusInWindow();
    }

    public void goToFreeplay(){
        if(tmm != null) tmm=null;
        if(tp != null) tp=null;
        fm = new FreeplayPanel();
        getContentPane().removeAll();
        getContentPane().add(fm, BorderLayout.CENTER);
        revalidate();
        fm.requestFocusInWindow();
    }
}
