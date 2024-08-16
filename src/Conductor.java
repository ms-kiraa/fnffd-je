import java.util.HashMap;
import java.util.Map;

public class Conductor {
    public static double lastTime = 0;

    public static double time = 0;
    public static double bpm = 60.0;
    // idk what rate is so im skipping it

    public static double crochet;
    public static double crochetSec;

    public static double beatDb;
    public static int beat;

    public static double stepDb;
    public static int step;

    public static Conductor instance;

    public static Map<String, Runnable> everyBeat = new HashMap<>();
    public static Map<String, Runnable> everyStep = new HashMap<>();

    public Conductor(){
        instance = this;
    }

    public static double setTime(double newTime){
        lastTime = time;
        time = newTime;
        return newTime;
    }

    public static double getTime(){
        return time;
    }

    public static double getCrochet(){
        return getCrochetSec() * 2;
    }

    public static double getCrochetSec(){
        return 60 / bpm;
    }

    public static double getBeatDb(){
        return time / getCrochet();
    }

    public static int getBeat(){
        return (int)Math.floor(getBeatDb());
    }

    public static double getStepDb(){
        return getBeatDb()*4;
    }

    public static int getStep(){
        return (int)Math.floor(getStepDb());
    }
}
