import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends JFrame {

    public final static int windowWidth = 815;
    public final static int windowHeight = 833;

    public static Main main;

    // this is probably a bad idea but meh
    public static RecordScratchScreen rss;
    public static MainMenu mm;
    public static Stage s;

    public static final int targetFPS = 9999;
    public static final int TICK_TIME = 17;

    private Main(String title){
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
                //ClientPrefs.saveAllData();
                setVisible(false);
                dispose();
                System.exit(0);
            }
        });

        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(new BorderLayout());
        setResizable(false);

        if(!offset){
            getContentPane().add(new SplashPanel(), BorderLayout.CENTER);
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
        TitlePanel tp = new TitlePanel();
        getContentPane().removeAll();
        getContentPane().add(tp, BorderLayout.CENTER);
        revalidate();
        tp.requestFocusInWindow();
    }

    /**
     * Creates the TitleScreen object and adds it to the frame. 
     * <p>
     * Since it is only meant to be accessed through the RecordScratchScreen, it only nulls that.
     * 
     * @see TitleScreen
     * @see RecordScratchScreen
     * @deprecated succeeded by TitlePanel
     * @see TitlePanel
     */
    public void goToTitleScreen(){
        TitleScreen ts = new TitleScreen();
        getContentPane().remove(rss);
        rss = null;
        getContentPane().add(ts, BorderLayout.CENTER);
        revalidate();
        ts.requestFocusInWindow();
    }

    /**
     * Switches current panel to be the main menu and nulls all other non-null menu panels.
     * <p>
     * If the main menu object is currently null, it creates a new one.
     * @see MainMenu
     */
    public void goToMainMenu(){
        if(mm == null) mm = new MainMenu();
        getContentPane().removeAll();
        // todo: if options is not null make options null, same with game i think
        getContentPane().add(mm, BorderLayout.CENTER);
        mm.addBinds();
        revalidate();
        FadeManager.cancelFade();
        FadeManager.fadeIn(Color.BLACK, 1, 1);
        mm.requestFocusInWindow();
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {main = new Main("fnf free download: java edition"); System.out.println(main.getWidth());});
        
    }
}
