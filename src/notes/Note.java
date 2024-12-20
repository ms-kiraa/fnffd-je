package notes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import backend.Camera;
import backend.imagemanip.ImageEffect;
import objects.fx.FXGameObject;

/**
 * The base note class. Comes with variables for controlling notes
 */
public class Note extends FXGameObject {
    protected static Map<String, BufferedImage> cache = new HashMap<>();
    public boolean playerNote;
    public Direction dir;
    public boolean fumpNote;

    public Note(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, 1.3, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), camera);
        this.dir = direction;
        this.playerNote = playerNote;
        this.fumpNote = fump;
        this.scrollFactor = 0;
    }
    public Note(double x, double y, Direction direction, Camera camera, boolean playerNote){
        super(x, y, 1.3, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), camera);
        this.dir = direction;
        this.playerNote = playerNote;
        this.fumpNote = false;
        this.scrollFactor = 0;
    }

    public Note(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, 1.3, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), camera);
        this.dir = Direction.getIntAsDirection(direction);
        this.playerNote = playerNote;
        this.fumpNote = fump;
        this.scrollFactor = 0;
    }
    public Note(double x, double y, int direction, Camera camera, boolean playerNote){
        super(x, y, 1.3, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), camera);
        this.dir = Direction.getIntAsDirection(direction);
        this.playerNote = playerNote;
        this.fumpNote = false;
        this.scrollFactor = 0;
    }

    protected BufferedImage getImageFromCache(String path){
        try{
            if(!cache.containsKey(path)){
                BufferedImage load = ImageIO.read(new File(path));
                for(ImageEffect e : effects) {
                    load = e.apply(load);
                }
                cache.put(path, load);
            }
            return cache.get(path);
        } catch(Exception e){
            System.out.println(path);
            e.printStackTrace();
            //System.exit(0);
        }
        return null;
    }

    /**
     * An enum to hold all the different formats for getting a direction as a string
     */
    public enum CapsMode{
        ALL_LOWERCASE, UPPER_CAMEL_CASE, ALL_UPPERCASE
    }
    /**
     * An enum to hold all the possible directions a note can have. Also contains some manipulation/formatting methods
     */
    public enum Direction{
        LEFT,DOWN,UP,RIGHT;
        // -- DIRECTION FORMATTING METHODS -- \\
    
        /**
         * Formats the direction variable into text with capitalization based on the inputted {@link CapsMode}
         * @param dir direction to format
         * @param capsMode how to format the direction
         * @return 
         */
        public String getDirectionAsString(CapsMode capsMode){
            String returnString = "null";

            switch(this){
                case LEFT:
                    returnString = "left";
                    break;
                case DOWN:
                    returnString = "down";
                    break;
                case UP:
                    returnString = "up";
                    break;
                case RIGHT:
                    returnString = "right";
                    break;
            }

            switch(capsMode){
                case UPPER_CAMEL_CASE:
                    returnString = returnString.substring(0,1).toUpperCase() + returnString.substring(1);
                    break;
                case ALL_UPPERCASE:
                    returnString = returnString.toUpperCase();
                    break;
                default:
                    // do nothing
                    break;
            }
            return returnString;
        }

        public int getDirectionAsInt(){
            switch(this){
                case LEFT:
                    return 0;
                case DOWN:
                    return 1;
                case UP:
                    return 2;
                case RIGHT:
                    return 3;
            }
            return -1;
        }

        public static Direction getIntAsDirection(int direction){
            switch(direction){
                case 0:
                    return Direction.LEFT;
                case 1:
                    return Direction.DOWN;
                case 2:
                    return Direction.UP;
                case 3:
                    return Direction.RIGHT;
            }
            return null;
        }
    }
}