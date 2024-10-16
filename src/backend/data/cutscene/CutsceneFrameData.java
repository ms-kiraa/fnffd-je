package backend.data.cutscene;

import java.awt.image.BufferedImage;

public class CutsceneFrameData {
    public String text;
    public CutsceneCharacterData speaker; // who's talking
    public BufferedImage frameImg; // what to show as the background, or null if keep last
    public String song; // song that plays upon proceeding to the frame, or null if keep last
    // do frame events laterrrr

    public CutsceneFrameData(String text, CutsceneCharacterData speaker, BufferedImage frameImg, String song) {
        this.text = text;
        this.speaker = speaker;
        this.frameImg = frameImg;
        this.song = song;
    }

    public CutsceneFrameData(String text, CutsceneCharacterData speaker, BufferedImage frameImg) {
        this(text, speaker, frameImg, null);
    }

    public CutsceneFrameData(String text, CutsceneCharacterData speaker) {
        this(text, speaker, null, null);
    }
}
