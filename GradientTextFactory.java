package backend.imagemanip;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class GradientTextFactory {

    private GradientTextFactory(){}

    public static BufferedImage makeGradientText(String text, Font font, Color topColor, Color bottomColor) {
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(font);
        int textWidth = fm.stringWidth(text);
        int textDescent = fm.getDescent();
        int textHeight = textDescent*3;

        BufferedImage gradientText = new BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = gradientText.createGraphics();

        GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, textHeight, bottomColor);

        g2d.setFont(font);
        g2d.setPaint(gradient);

        g2d.drawString(text, 0, textHeight/2+textHeight/3);

        g2d.dispose();

        // this is dumb
        gradientText = ImageUtils.removeBlankSpace(gradientText);

        
        //gradientText = ImageUtils.removeBlankSpace(gradientText);

        try {
            ImageIO.write(gradientText, "png", new File("hi.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gradientText;
    }
}
