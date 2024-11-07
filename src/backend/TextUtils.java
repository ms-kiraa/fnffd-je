package backend;

import java.awt.Color;
import java.awt.Graphics;

public class TextUtils {
    private TextUtils(){}

    public static void drawOutlinedText(Graphics g, String str, int x, int y, int thickness) {
        drawOutlinedText(g, str, x, y, thickness, Color.BLACK);
    }
    public static void drawOutlinedText(Graphics g, String str, int x, int y, int thickness, Color outlineColor) {
        Color originalColor = g.getColor();
        g.setColor(outlineColor);
    
        // Draw the outline around the text by spreading the text horizontally and vertically
        for (int i = -thickness; i <= thickness+1; i++) {
            for (int j = -thickness; j <= thickness+2; j++) {
                if (i != 0 || j != 0) {  // Ensure that we don't draw the text at (0, 0), which would overlap the main text
                    g.drawString(str, x + i, y + j);
                }
            }
        }
    
        // Restore the original color and draw the main text
        g.setColor(originalColor);
        g.drawString(str, x, y);
    }
}
