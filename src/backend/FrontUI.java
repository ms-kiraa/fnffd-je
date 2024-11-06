package backend;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.JComponent;

import main.Main;

public class FrontUI extends JComponent {
    public FrontUI(){}

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(Main.main.debugUIEnabled) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();

            Map<String, String> debugInfo = Main.main.panelSpecificDebugInfo;

            if(debugInfo.keySet().size() > 0) {
                int ymod = 50;
                for(String key : debugInfo.keySet()) {
                    String draw = key + ": " + debugInfo.get(key);
                    g.drawString(draw, Main.windowWidth-50 - fm.stringWidth(draw), ymod);
                    ymod += 50;
                }
            }
        }
        
        
    }
}
