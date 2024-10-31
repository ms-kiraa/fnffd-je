package backend;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

// i WOOOULD call this "Paths.java" but fucking java.nio.file.Paths TOOK IT FIRST GOD DAMN MY CHUD LIFE
public class FileRetriever {
    private FileRetriever(){}
    public static BufferedImage image(String imagePath) {
        File image = null;
        if(imagePath.endsWith(".png")) {
            image = new File(imagePath);
        } else {
            image = new File("./" + imagePath + ".png");
        }

        if(image.exists()) {
            try{
                return ImageIO.read(image);
            } catch(Exception e) {
                System.out.println("SOMETHING WENT WRONG LOADING " + imagePath + ": " + e.getMessage());
            }
        } else {
            System.out.println(imagePath + " IS NULL NOOOOO");
        }

        return null;
    }
}
