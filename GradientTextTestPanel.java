package panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import backend.MusicBeatPanel;
import backend.imagemanip.GradientTextFactory;

public class GradientTextTestPanel extends MusicBeatPanel {
    public GradientTextTestPanel(){super();}

    private BufferedImage[] texts;
    int maxWeeks;

    @Override
    protected void create(){
        setBackground(Color.BLACK);
        /*
         *     var shit=make_colour_hsv((225/weeks+1)*i,219,188)
         *     var shit2=make_colour_hsv((225/weeks+1)*i+5,235,120)
         */
        maxWeeks = Integer.MAX_VALUE/100;
        Font fnt = new Font("Comic Sans MS", Font.PLAIN, 36);
        texts = new BufferedImage[maxWeeks+1];
        for(int i = 0; i <= maxWeeks; i++) {
            Color c1 = Color.getHSBColor(((225/maxWeeks+1)*i)/255f, 219/255f, 188/255f);
            Color c2 = Color.getHSBColor(((225/maxWeeks+1)*i+5)/255f, 235/255f, 120/255f);
            texts[i] = GradientTextFactory.makeGradientText(((i == 0) ? "TUTORIAL" : "WEEKND " + i), fnt, c1, c2);
        }

        super.create();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        for(int i = 0; i <= maxWeeks; i++) {
            g.drawImage(texts[i], 0, (texts[i].getHeight()*2)*i, texts[i].getWidth()*2, (texts[i].getHeight()*2), null);
        }
    }
}
