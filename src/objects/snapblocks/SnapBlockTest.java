package objects.snapblocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;

import backend.FileRetriever;
import backend.MusicBeatPanel;
import backend.imagemanip.ImageUtils;

public class SnapBlockTest extends MusicBeatPanel {
    public SnapBlockTest(){super();}

    BufferedImage template;

    BufferedImage head;
    BufferedImage loop;
    BufferedImage end;

    int len = 0;
    String text = "this is an example of auto-resizing";

    int textidx = 0;

    @Override
    protected void create(){
        template = FileRetriever.image("img/ui/snapblock");

        template = ImageUtils.colorMultiply(template, Color.RED);

        head = template.getSubimage(0, 0, 64, template.getHeight());
        loop = template.getSubimage(64, 0, 1, template.getHeight());
        end = template.getSubimage(65, 0, template.getWidth()-65, template.getHeight());

        String[] texts = {
            "this is an example of auto-resizing", 
            "the game calculates how long the block should be", 
            "and sizes it to fit the text", 
            "it can end up being very long, there's no real hard cap on how long they can go",
            "it can end up being very long, there's no real hard cap on how long they can goo",
            "it can end up being very long, there's no real hard cap on how long they can goooo",
            "it can end up being very long, there's no real hard cap on how long they can gooooooo",
            "it can end up being very long, there's no real hard cap on how long they can gooooooooooo",
            "it can end up being very long, there's no real hard cap on how long they can goooooooooooooooo",
            "it can end up being very long, there's no real hard cap on how long they can goooooooooooooooooooooo",
            "but the inverse is also true, they can be very",
            "s",
            "h",
            "o",
            "r",
            "t"
        };
        text = texts[0];
        
        setLayout(null);
        JComboBox<String> jcb = new JComboBox<String>();
        jcb.addItem("for this frame");
        jcb.addItem("indefinitely");

        jcb.setBounds(100, 100, 400, 70);
        add(jcb);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right");
        this.getActionMap().put("right", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                len += 10;
                textidx += 1;
                text = texts[textidx];
            }
            
        });

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left");
        this.getActionMap().put("left", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                len -= 10;
                if(len < 0) len = 0;
            }
            
        });

        super.create();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        double size = 1.5;
        int x = 0;
        g.drawImage(head, 0, 0, (int)(head.getWidth()*size), (int)(head.getHeight()*size), null);
        x += head.getWidth()*size;

        int textX = x;

        // calculate how long the block should be
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, (int)(18*size)));
        FontMetrics fm = g.getFontMetrics();

        int len = fm.stringWidth(text);

        for(int i = 0; i <= len; i++) {
            g.drawImage(loop, x, 0, (int)(loop.getWidth()), (int)(loop.getHeight()*size), null);
            x += 1;
        }
        g.drawImage(end, x, 0, (int)(end.getWidth()*size), (int)(end.getHeight()*size), null);

        g.drawString(text, textX, (int)(fm.getHeight()*1.9)-fm.getDescent());

    }
}
