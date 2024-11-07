package backend.imagemanip;

import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

// this is a bad way to do it
// too bad
public class Bar {

    // static variables
    public static final int PUSH_FILL_IN = 0;
    public static final int PUSH_OUTLINE_OUT = 1;

    // instance variables
    public Color fillColor;
    public Color outlineColor;

    public int width;
    public int height;
    public int outlineThickness;

    public boolean dropShadow;
    public float shadowWeight;

    public int shadowOffsetX;
    public int shadowOffsetY;

    public double fillPercent = 0.5;

    private int fillMode;

    public Bar(Color outlineColor, Color fillColor, int width, int height, int outlineThickness, boolean dropShadow, float shadowWeight, int shadowOffsetX, int shadowOffsetY) {
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
        this.outlineThickness = outlineThickness;
        
        this.width = width;
        this.height = height;

        this.dropShadow = dropShadow;
        this.shadowWeight = shadowWeight;
        
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;

        this.fillMode = PUSH_FILL_IN;
    }

    public Bar(Color outlineColor, Color fillColor, int width, int height, int outlineThickness, boolean dropShadow, float shadowWeight) {
        this(fillColor, outlineColor, width, height, outlineThickness, dropShadow, shadowWeight, 3, 3);
    }

    public Bar(Color outlineColor, Color fillColor, int width, int height, int outlineThickness, boolean dropShadow) {
        this(fillColor, outlineColor, width, height, outlineThickness, dropShadow, 0.5f);
    }

    public Bar(Color outlineColor, Color fillColor, int width, int height, int outlineThickness) {
        this(fillColor, outlineColor, width, height, outlineThickness, false);
    }

    public Bar(int width, int height) {
        this(Color.BLACK, Color.WHITE, width, height, 5);
    }


    public Bar setFillMode(int fillMode) {
        if(fillMode == PUSH_FILL_IN || fillMode == PUSH_OUTLINE_OUT) {
            this.fillMode = fillMode;
        }
        return this;
    }

    public BufferedImage makeBar() {
        int barWidth = width;
        int barHeight = height;
        if(fillMode == PUSH_OUTLINE_OUT) {
            barWidth += outlineThickness * 2;
            barHeight += outlineThickness * 2;
        }

        int imgWidth = barWidth;
        int imgHeight = barHeight;
        if(dropShadow) {
            imgWidth += shadowOffsetX;
            imgHeight += shadowOffsetY;
        }

        BufferedImage ret = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = ret.createGraphics();

        // draw shadow
        Composite prev = g2.getComposite();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowWeight);

        g2.setColor(Color.BLACK);
        g2.setComposite(ac);
        g2.fillRect(shadowOffsetX, shadowOffsetY, barWidth, barHeight);
        g2.setComposite(prev);

        // draw base
        g2.setColor(outlineColor);
        g2.fillRect(0, 0, barWidth, barHeight);

        // draw fill
        int fillWidth = width;
        int fillHeight = height;
        if(fillMode == PUSH_FILL_IN) {
            fillWidth -= outlineThickness * 2;
            fillHeight -= outlineThickness * 2;
        }

        g2.setColor(fillColor);
        g2.fillRect(outlineThickness, outlineThickness, (int)(fillWidth*fillPercent), fillHeight);

        g2.dispose();

        try {
            ImageIO.write(ret, "png", new File("funny.png"));
        } catch (Exception e) {
            // TODO: handle exception
        }

        return ret;
    }
}
