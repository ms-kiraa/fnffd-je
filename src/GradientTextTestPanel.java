import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GradientTextTestPanel extends MusicBeatPanel {
    public GradientTextTestPanel(){super();}

    private BufferedImage text;

    @Override
    protected void create(){
        setBackground(Color.BLACK);
        /*
         *     var shit=make_colour_hsv((225/weeks+1)*i,219,188)
         *     var shit2=make_colour_hsv((225/weeks+1)*i+5,235,120)
         */
        int week = 1;
        text = GradientTextFactory.makeGradientText("WEEKND " + week, new Font("Comic Sans MS", Font.PLAIN, 36), Color.getHSBColor(((225/4)*week)/255f, 219/255f, 118/255f), Color.getHSBColor(((225/4)*week)/255f+(5/255f), 235/255f, 120/255f));

        super.create();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(text, 0, 0, text.getWidth()*2, text.getHeight()*2, null);
    }
}
