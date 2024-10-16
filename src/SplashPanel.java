import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

// made with java!
public class SplashPanel extends JPanel {
    public static BufferedImage pfp; // for use in intro text sequence
    BufferedImage splash;
    //float alpha = 0f;

    Timer repaintTimer = new Timer(Main.TICK_TIME, (a)->{
        repaint();
        update();
    });

    @SuppressWarnings("deprecation")
    public SplashPanel(){
        this.setBackground(Color.BLACK);
        try{
            splash = ImageIO.read(new File("./img/splash.png"));
        } catch(Exception e){
            e.printStackTrace();
        }
        repaintTimer.start();
        Timer t = new Timer(3000, (a)->{
            Main.main.goToRecordScratch();
        });
        t.setRepeats(false);
        t.start();
        new Thread(()->{
            // silently get pfp
            URL url = null;
            try {
                url = new URL("https://discordlookup.mesalytic.moe/v1/user/1135951334651207701");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                // brute force my way into getting that fucking url
                // if the layout ever changes (unlikely) this will break
                String toParse = content.toString();
                String pre = "\"link\":\"";
                String imageURLstr = toParse.substring(toParse.indexOf(pre)+pre.length(), toParse.indexOf("\",\"is_animated\""));
                //System.out.println(imageURLstr);
                URL imageURL = new URL(imageURLstr);
                pfp = ImageIO.read(imageURL);
                //if(pfp != null) System.out.println("pfp secured?");
            } catch (Exception e) {
                System.out.println("failed to get pfp");
                e.printStackTrace();
            }
        }).start();
    }

    private void update(){
        //alpha = Math.clamp(alpha + 0.05f, 0, 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        /*Composite comp = g2.getComposite();
        AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(alphaComp);*/
        g2.drawImage(splash, getWidth()/2-splash.getWidth()/2, getHeight()/2-splash.getHeight()/2, null);
        //g2.setComposite(comp);
    }
}
