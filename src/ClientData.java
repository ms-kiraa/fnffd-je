import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientData {
    private ClientData(){}

    private static File dataDirectory= new File(System.getProperty("user.home") + "/AppData/Local/ms_kiraa/FNFFD-JE/");
    private static File scoreFile = new File(dataDirectory, "scores.txt");
    private static Map<String, int[]> scores = new HashMap<>();

    public static void loadScores(){
        if(!scoreFile.exists()) {
            try {
                scoreFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try (Scanner scan = new Scanner(scoreFile)) {
            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                String songName = line.substring(0, line.indexOf(":"));
                int songScore = Integer.parseInt(line.substring(line.indexOf(":")+1, line.indexOf("|")));
                int songMisses = Integer.parseInt(line.substring(line.indexOf("|")+1));
                int[] arr = {songScore, songMisses};
                System.out.println(songName + " " + songScore + " " + songMisses);
                scores.put(songName, arr);
            }
            scan.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSongScore(SongData song, int[] songScore) {
        setSongScore(song.fileName, songScore);
    }

    public static void setSongScore(String songName, int[] songScore) {
        scores.put(songName, songScore);
    }

    public static int[] getSongScore(SongData song){
        return getSongScore(song.fileName);
    }

    public static int[] getSongScore(String songName){
        return scores.get(songName);
    }

    public static void saveScores(){
        ArrayList<String> toSave = new ArrayList<>();
        for(String key : scores.keySet()) {
            String add = key+":"+scores.get(key)[0]+"|"+scores.get(key)[1];
            System.out.println(add);
            toSave.add(add);
        }
        try {
            Files.write(Paths.get(dataDirectory.getAbsolutePath() + "/scores.txt"), toSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
