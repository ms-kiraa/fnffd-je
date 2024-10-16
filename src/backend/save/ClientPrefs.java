package backend.save;

import java.awt.event.KeyEvent;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import effects.DudeSkins;

public class ClientPrefs {

    // location of save data
    private static File saveDataDirectory = new File(System.getProperty("user.home") + "/AppData/Local/ms_kiraa/FNFFD-JE/");
    // holds al lthe settings and stuff :)
    public static Map<String, String> settings = new HashMap<>();
    // chosen dude skin
    public static DudeSkins dudeSkin = DudeSkins.Default;

    // custom dude skin values
    public static ArrayList<ArrayList<Integer>> customFromValues = null;
    public static ArrayList<ArrayList<Integer>> customToValues = null;

    // note colors
    public static ArrayList<ArrayList<Integer>> noteColors = null;

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
            List<String> sets = Arrays.asList("downscroll:false", "left_bind:"+KeyEvent.VK_LEFT, "down_bind:"+KeyEvent.VK_DOWN, "up_bind:"+KeyEvent.VK_UP, "right_bind:"+KeyEvent.VK_RIGHT, "note_debug:false", "dude_skin:"+DudeSkins.values()[0]);

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
            boolean loadingFromValues = false, loadingToValues = false, loadingNoteColors = false;
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                String[] split = line.split(":");
                //System.out.println(split[0]);
                if(line.equals("[NOTE COLORS]")) {
                    noteColors = new ArrayList<>();
                    loadingNoteColors = true;
                    //System.out.println("adding note colors");
                    continue;
                } else if(line.equals("[CUSTOM DUDE SKIN FROM VALUES]")) {
                    customFromValues = new ArrayList<>();
                    loadingNoteColors = false;
                    loadingFromValues = true;
                    //System.out.println("adding from values");
                    continue;
                } else if(line.equals("[CUSTOM DUDE SKIN TO VALUES]")){
                    customToValues = new ArrayList<>();
                    loadingFromValues = false;
                    loadingToValues = true;
                    //System.out.println("adding to values");
                    continue;
                }
                if(!loadingFromValues && !loadingToValues && !loadingNoteColors){
                    if(split[0].equals("dude_skin")){
                        dudeSkin = DudeSkins.valueOf(split[1]);
                    } else if(!line.startsWith("[")){
                        settings.put(split[0], split[1]);
                    }
                } else {
                    String[] vals = line.split(",", 0);
                    ArrayList<Integer> add = new ArrayList<>();
                    for(String val : vals) {
                        add.add(Integer.parseInt(val));
                    }
                    if(loadingFromValues) {
                        customFromValues.add(add);
                    } else if(loadingToValues) {
                        customToValues.add(add);
                    } else if(loadingNoteColors) {
                        noteColors.add(add);
                    }
                }
            }
            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String set(String setting, Object value){
        settings.put(setting, value.toString());
        String curValue = settings.get(setting);
        return curValue;
    }

    public static boolean getBoolean(String setting){
        return Boolean.parseBoolean(settings.get(setting));
    }

    public static int[] getBinds(){
        int[] binds = {Integer.parseInt(settings.get("left_bind")), Integer.parseInt(settings.get("down_bind")), Integer.parseInt(settings.get("up_bind")), Integer.parseInt(settings.get("right_bind"))};
        return binds;
    }

    public static void saveAllSets(){
        ArrayList<String> sets = new ArrayList<>();
        for(String key : settings.keySet()){
            String add = key+":"+settings.get(key);
            sets.add(add);
            //System.out.println(add);
        }
        sets.add("dude_skin:"+dudeSkin);
        if(noteColors != null) {
            sets.add("[NOTE COLORS]");
            for(ArrayList<Integer> color : noteColors) {
                sets.add(color.get(0)+","+color.get(1)+","+color.get(2));
            }
        }
        if(customFromValues != null && customToValues != null) {
            sets.add("[CUSTOM DUDE SKIN FROM VALUES]");
            for(ArrayList<Integer> color : customFromValues) {
                sets.add(color.get(0)+","+color.get(1)+","+color.get(2));
            }
            sets.add("[CUSTOM DUDE SKIN TO VALUES]");
            for(ArrayList<Integer> color : customToValues) {
                sets.add(color.get(0)+","+color.get(1)+","+color.get(2));
            }
        }
        try {
            Files.write(Paths.get(saveDataDirectory.getAbsolutePath() + "/settings.txt"), sets);
        } catch (Exception e) {
            System.err.println("Failed to save settings");
            e.printStackTrace();
        }
    }
}
