package backend.data.cutscene;

import java.awt.Color;

public class CutsceneCharacterData {
    public String name;
    public Color color; // color to render name and text as
    public String dialogueSound; // what to play every text blip, or null if none

    // default speakers
    public static CutsceneCharacterData dude = new CutsceneCharacterData("Dude"); // fill out more nuanced info later when i have the game in front of me
    public static CutsceneCharacterData lady = new CutsceneCharacterData("Lady");
    public static CutsceneCharacterData strad = new CutsceneCharacterData("Strad");

    public CutsceneCharacterData(String name, Color color, String dialogueSound) {
        this.name = name;
        this.color = color;
        this.dialogueSound = dialogueSound;
    }

    public CutsceneCharacterData(String name, Color color) {
        this(name, color, null);
    }

    public CutsceneCharacterData(String name) {
        this(name, Color.WHITE, null);
    }
}
