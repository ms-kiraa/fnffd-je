import javax.swing.*;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends JFrame {

    public static int windowWidth = 815;
    public static int windowHeight = 833;

    public static Main main;

    // this is probably a bad idea but meh
    public static RecordScratchScreen rss;
    public static TitleScreen ts;
    public static MainMenu mm;
    public static Stage s;

    public static final int targetFPS = 60;
    public static final int TICK_TIME = 1000/targetFPS;

    private Main(String title){
        super(title);
        String load = "false";
        try{
            load = Files.readString(Paths.get("./LOAD_OFFSET_EDITOR.txt"));
        } catch(Exception e){
            e.printStackTrace();
        }
        boolean offset = Boolean.parseBoolean(load);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(new BorderLayout());
        setResizable(false);

        if(!offset){
            s = new Stage();
            getContentPane().add(s, BorderLayout.CENTER);
        } else {
            getContentPane().add(new AnimationOffsetPanel(), BorderLayout.CENTER);
        }

        pack();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // panel switching methods

    /**
     * Creates the TitleScreen object and adds it to the frame. 
     * <p>
     * Since it is only meant to be accessed through the RecordScratchScreen, it only nulls that.
     * 
     * @see TitleScreen
     * @see RecordScratchScreen
     */
    public void goToTitleScreen(){
        ts = new TitleScreen();
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
        SwingUtilities.invokeLater(() -> {main = new Main("fnf free download: java edition");});
    }
}
