import java.awt.event.KeyEvent;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientPrefs {

    // location of save data
    private static File saveDataDirectory = new File(System.getProperty("user.home") + "/AppData/Local/ms_kiraa/FNFFD-JE/");
    // holds al lthe settings and stuff :)
    public static Map<String, String> settings = new HashMap<>();

    // make it non-instantiatable
    private ClientPrefs(){}

    public static void init(){
        // run checks for data existing
        if(!saveDataDirectory.exists()){
            saveDataDirectory.mkdirs();
            System.out.println("made save data directory");
        }
        boolean found = false;
        for(File f : saveDataDirectory.listFiles()){
            if(f.getName().equals("settings.txt")) {
                found = true;
                break;
            }
        }
        if(!found){
            // set up default settings
            List<String> sets = Arrays.asList("downscroll:false", "left_bind:"+KeyEvent.VK_LEFT, "down_bind:"+KeyEvent.VK_DOWN, "up_bind:"+KeyEvent.VK_UP, "right_bind:"+KeyEvent.VK_RIGHT);

            // save default settings
            try {
                Files.write(Paths.get(saveDataDirectory.getAbsolutePath() + "/settings.txt"), sets);
                System.out.println("made settings");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // now that data (probably) exists, we can attempt to load it
        try (Scanner scan = new Scanner(new File(saveDataDirectory.getAbsolutePath() + "/settings.txt"))) {
            while(scan.hasNextLine()){
                String[] split = scan.nextLine().split(":");
                System.out.println(split[0]);
                settings.put(split[0], split[1]);
            }
            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getDownscroll(){
        return Boolean.parseBoolean(settings.get("downscroll"));
    }
}
