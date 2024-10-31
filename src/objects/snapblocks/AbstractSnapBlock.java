package objects.snapblocks;

import backend.Camera;
import backend.FileRetriever;
import objects.GameObject;

public abstract class AbstractSnapBlock extends GameObject {
    public AbstractSnapBlock(double x, double y, Camera cam) {
        super(x, y, 1, FileRetriever.image("ui/snapblock"), cam);
    }
}
