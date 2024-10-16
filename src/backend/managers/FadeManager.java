package backend.managers;

import java.awt.Color;
import java.awt.Graphics2D;

import main.Main;

public class FadeManager {
    public static int alpha = 255;
    public static boolean fading = false;
    public static Color color;

    public static void fadeOut(Color fadeColor, int rate, int pauseMS){
        cancelFade();
        Thread th = new Thread(() ->{
            color = fadeColor;
            fading = true;
            alpha = 0;
            while(alpha < 255){
                alpha += rate;
                try{
                    Thread.sleep(pauseMS);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            alpha = 255;
            //fading = false;
        });
        th.setName("Fade Out Thread");
        th.start();
    }

    public static void fadeOut(Color fadeColor, int rate, int pauseMS, boolean closeOnComplete){
        cancelFade();
        Thread th = new Thread(() ->{
            color = fadeColor;
            fading = true;
            alpha = 0;
            while(alpha < 255){
                alpha += rate;
                try{
                    Thread.sleep(pauseMS);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            alpha = 255;
            if(closeOnComplete) System.exit(1);
        });
        th.setName("Fade Out Thread");
        th.start();
    }

    public static void fadeOut(Color fadeColor, int rate, int pauseMS, Runnable run){
        cancelFade();
        Thread th = new Thread(() ->{
            color = fadeColor;
            fading = true;
            alpha = 0;
            while(alpha < 255){
                alpha += rate;
                try{
                    Thread.sleep(pauseMS);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            alpha = 255;
            run.run();
            //fading = false;
        });
        th.setName("Fade Out Thread");
        th.start();
    }

    public static void fadeIn(Color fadeColor, int rate, int pauseMS){
        cancelFade();
        Thread th = new Thread(() ->{
            color = fadeColor;
            fading = true;
            alpha = 255;
            while(alpha > 0){
                alpha -= rate;
                try{
                    Thread.sleep(pauseMS);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            alpha = 0;
            fading = false;
        });
        th.setName("Fade In Thread");
        th.start();
    }

    public static void cancelFade(){
        alpha = 0;
        fading = false;
    }

    private static int clamp(int input, int min, int max){
        if(input > max) return max;
        if(input < min) return min;
        return input;
    }


    public static void drawSelf(Graphics2D g){
        if(fading){
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), clamp(alpha, 0, 255));

            Color dum = g.getColor();
            g.setColor(c);

            g.fillRect(0,0,Main.windowWidth, Main.windowHeight);

            g.setColor(dum);
        }
    }


}
