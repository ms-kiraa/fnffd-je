import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class SongData {
    // holds info about a song

    private static File songFolder = new File("./songs"); // folder that songs reside in
    public File folder; // folder that the song's data resides in

    public String formalName; // what the song's actual name is, e.g. summer
    public String fileName; // what the song's filename is, e.g. mus_w1s1

    public String dudeChar; // who you play as
    public String badguyChar; // who you fight
    public String ladyChar; // who is lady. who is she. help. i dont know this woman. help. help. help.

    public String stage; // what the song's background is called to index in "stages/"

    public String noteskin; // unimplemented, but will force certain note graphics to be used, such as the game and watch notes in i, robot

    public BufferedImage icon; // freeplay icon

    public SongData(String folderName) {
        // load song info
        folder = new File(songFolder, folderName);
        if(!folder.exists()) {
            throw new IllegalArgumentException("Could not find song with folder name "+folderName);
        }
        fileName = folder.getName();
        try{
            icon = ImageIO.read(new File(folder, "icon.png"));
        }catch(Exception e){
            e.printStackTrace();
        }
        File ld = new File(folder, "data.txt");
        try(Scanner scan = new Scanner(ld)){
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                String[] split = line.split(":");

                switch (split[0]) {
                    case "name":
                        formalName = split[1];
                        break;
                    case "dude-char":
                        dudeChar = split[1];
                        break;
                    case "badguy-char":
                        badguyChar = split[1];
                        break;
                    case "lady-char":
                        ladyChar = split[1];
                        break;
                    case "stage":
                        stage = split[1];
                        break;
                    case "noteskin":
                        noteskin = split[1];
                        break;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        // i really do wonder if there's a better way to do this
        if(formalName == null) formalName = "null";
        if(dudeChar == null) dudeChar = "dude";
        if(badguyChar == null) badguyChar = "strad";
        if(ladyChar == null) ladyChar = "lady";
        if(stage == null) stage = "stage";
        if(noteskin == null) noteskin = "default";
    }

    public static SongData[] loadAllSongs(){
        SongData[] ret;
        File[] songs = songFolder.listFiles();

        ArrayList<SongData> retList = new ArrayList<>();
        for(int i = 0; i < songs.length; i++) {
            try{
                SongData add = new SongData(songs[i].getName().replaceFirst("[.][^.]+$", ""));
                retList.add(add);
            } catch (IllegalArgumentException iae){
                System.err.println("Got invalid song " + songs[i].getName() + "; ignoring");
            }
        }
        ret = new SongData[retList.size()];
        retList.toArray(ret);
        return ret;
    }
}
