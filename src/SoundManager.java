import java.io.File;

import javax.sound.sampled.*;

public class SoundManager {
    public static Clip songClip;
    public static Clip extraSongClip;
    public static Clip sfxClip;

    public static void playSFX(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            sfxClip = AudioSystem.getClip();
            sfxClip.open(audioIn);
            sfxClip.start();
            
            sfxClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(soundFile);
        }
    }

    public static void loopExtraSound(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            extraSongClip = AudioSystem.getClip();
            extraSongClip.open(audioIn);
            extraSongClip.start();

            extraSongClip.loop(Clip.LOOP_CONTINUOUSLY);
            
            extraSongClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP) {
                        extraSongClip.close();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(soundFile);
        }
    }
    
}
