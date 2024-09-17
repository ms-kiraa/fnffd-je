import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

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
            System.err.println("Could not find song with folder name "+folderName);
            return;
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
    }

    public static SongData[] loadAllSongs(){
        SongData[] ret;
        File[] songs = songFolder.listFiles();
        ret = new SongData[songs.length];
        for(int i = 0; i < songs.length; i++){
            ret[i] = new SongData(songs[i].getName());
        }
        return ret;
    }
}
