import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;

// technically this could be done with colorreplaceeffect but this is just more convenient
public class DudeEyeEffect implements ImageEffect {
    public Color eyeColor = new Color(32, 30, 40);
    public Color replaceColor = new Color(255, 255 ,255);

    public DudeEyeEffect(double opacity){
        replaceColor = new Color(255, 255, 255, (int)(255*opacity));
    }

    private BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
    
    @Override
    public BufferedImage apply(BufferedImage bi) {
        BufferedImage ret = copyImage(bi);
        BufferedImageOp lookup = new LookupOp(new ColorMapper(eyeColor, replaceColor), null);
        ret = lookup.filter(ret, null);
        return ret;
    }
}
