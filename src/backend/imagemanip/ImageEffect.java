package backend.imagemanip;

import java.awt.image.BufferedImage;

public interface ImageEffect {
    public BufferedImage apply(BufferedImage bi);
    public BufferedImage remove(BufferedImage bi);
}
