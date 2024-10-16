import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class CutsceneEditorPanel extends MusicBeatPanel {
    public CutsceneEditorPanel(){super();}

    BufferedImage ig;
    @Override
    protected void create(){
        try {
            ig = ImageIO.read(new File("./img/unknownframe.png"));
        } catch (Exception e) {
            // TODO: handle exception
        }
        super.create();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(ig, 0, 0, Main.windowWidth, Main.windowHeight, getFocusCycleRootAncestor());
    }

}
