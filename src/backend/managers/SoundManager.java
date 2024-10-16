package backend.managers;

import java.io.File;

import javax.sound.sampled.*;

public class SoundManager {
    public static Clip songClip = null;
    public static Clip extrasongClip = null;
    //public static Clip sfxClip = null;

    private static float masterGain = 1f;

    public static void playSFX(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(audioIn);
            sfxClip.start();
            setVolume(sfxClip, masterGain);
            
            sfxClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                }
            });

            setVolume(sfxClip, masterGain);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(soundFile);
        }
    }

    public static void playSFX(String soundFile, float gain) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(audioIn);
            sfxClip.start();
            setVolume(sfxClip, masterGain*gain);
            
            sfxClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                }
            });

            setVolume(sfxClip, masterGain);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(soundFile);
        }
    }

    public static void playSong(String soundFile, Runnable run) {
        try{
            File f = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());  
            songClip = AudioSystem.getClip();
            songClip.open(audioIn);
            songClip.start();
            songClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP){
                        if(run != null) run.run();
                    }   
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setSongPosition(long ms){
        songClip.stop();
        songClip.setMicrosecondPosition(ms);
        songClip.start();
    }

    public static void setSongLoop(boolean loop){
        songClip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
    }

    public static void loopExtraSound(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            extrasongClip = AudioSystem.getClip();
            extrasongClip.open(audioIn);
            extrasongClip.start();

            extrasongClip.loop(Clip.LOOP_CONTINUOUSLY);
            
            extrasongClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if (myLineEvent.getType() == LineEvent.Type.STOP) {
                        extrasongClip.close();
                    }
                }
            });

            setVolume(extrasongClip, masterGain);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(soundFile);
        }
    }

    public static void setVolume(float volume) {
        if (volume < 0f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        masterGain = volume;
        if(extrasongClip != null) setVolume(extrasongClip, volume);
        if(songClip != null) setVolume(songClip, volume);
        //if(sfxClip != null) setVolume(sfxClip, volume);
    }


    private static void setVolume(Clip clip, float volume){
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);        
        gainControl.setValue(20f * (float) Math.log10(volume));
    }
    
}
