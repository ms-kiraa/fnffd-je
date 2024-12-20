package backend.imagemanip;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ImageUtils {
    private ImageUtils(){}

    public static BufferedImage colorMultiply(BufferedImage input, Color color){
        int[] rgb = {color.getRed(), color.getGreen(), color.getBlue()};
        return colorMultiply(input, rgb);
    }

    public static BufferedImage colorMultiply(BufferedImage input, int[] rgb) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
        int[] inputPixels = input.getRGB(0, 0, width, height, null, 0, width);
        int[] outputPixels = new int[width * height];

        double redMult = rgb[0] / 255.0;
        double greenMult = rgb[1] / 255.0;
        double blueMult = rgb[2] / 255.0;

        for (int i = 0; i < inputPixels.length; i++) {
            int pixel = inputPixels[i];

            int alpha = (pixel >> 24) & 0xFF;
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;

            red = Math.min(255, (int) (red * redMult));
            green = Math.min(255, (int) (green * greenMult));
            blue = Math.min(255, (int) (blue * blueMult));

            outputPixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        ret.setRGB(0, 0, width, height, outputPixels, 0, width);
    
        return ret;
    }

    public static BufferedImage makeSolid(int width, int height, Color color) {
        BufferedImage solid = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int rgb = color.getRGB();

        // Access the pixel data array directly
        int[] pixels = ((DataBufferInt) solid.getRaster().getDataBuffer()).getData();
        
        // Fill the entire array with the color's RGB value
        Arrays.fill(pixels, rgb);
        
        return solid;
    }

    public static BufferedImage removeBlankSpace(BufferedImage image) {
        int minY = 0, maxY = 0, minX = Integer.MAX_VALUE, maxX = 0;
        boolean isBlank, minYIsDefined = false;
        WritableRaster raster = image.getRaster();
    
        for (int y = 0; y < image.getHeight(); y++) {
            isBlank = true;
    
            for (int x = 0; x < image.getWidth(); x++) {
                if (raster.getSample(x, y, 3) != 0) {
                    isBlank = false;
    
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }
    
            if (!isBlank) {
                if (!minYIsDefined) {
                    minY = y;
                    minYIsDefined = true;
                } else {
                    if (y > maxY) maxY = y;
                }
            }
        }
    
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
