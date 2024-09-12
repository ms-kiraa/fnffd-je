import javax.swing.JPanel;
import javax.swing.Timer;

public class MusicBeatPanel extends JPanel {
    Timer redraw = new Timer(Main.TICK_TIME, (a)->{
        update(0.0f);
        repaint();
    });

    protected Camera cam;

    protected int curStep = -1;
    protected int curBeat = -1;

    public MusicBeatPanel(){
        new Conductor();
        create();
    }

    /**
     * Starts the redraw/update timer and sets up camera
     * <p>
     * Should be called via <code>super.create()</code> at least once if overridden
     */
    protected void create(){
        cam = new Camera();
        redraw.start();
    }

    /**
     * Updates Conductor song position and calls <code>stepHit()</code> and <code>beatHit()</code> upon either changing
     * <p>
     * Should be called via <code>super.update(elapsed)</code> at least once if overridden
     * @param elapsed time since last update
     */
    protected void update(float elapsed){
        if(SoundManager.songClip != null){
            double songpos = (SoundManager.songClip.getMicrosecondPosition() / 1000000.0);
            Conductor.setTime(songpos);
        }
        // check step
        if(curStep != Conductor.getStep()) {
            curStep = Conductor.getStep();
            stepHit(curStep);
        }
        // check beat
        if(curBeat != Conductor.getBeat()) {
            curBeat = Conductor.getBeat();
            beatHit(curBeat);
        }
    }

    // condctor
    protected void stepHit(int curStep){}

    protected void beatHit(int curBeat){}
}
