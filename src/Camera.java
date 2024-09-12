import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Camera {
    private Map<String, List<GameObject>> layers;
    private Map<String, Integer> layerDepths;
    private ArrayList<String> orderToDraw;

    private Rectangle bounds;

    public double x;
    public double y;

    public double scaleFactor;
    private Color background;
    private BufferedImage bgBI;

    /**
     * Creates a new Camera object.
     * <p>
     * This process includes adding the default layers.
     * @param x starting X position of the camera.
     * @param y starting Y position of the camera.
     * @param scaleFactor starting scale of the camera.
     */
    public Camera(double x, double y, double scaleFactor) {
        this.x = x;
        this.y = y;
        this.scaleFactor = scaleFactor;

        bounds = new Rectangle();
        recalculateBounds();

        layers = new HashMap<>();
        layerDepths = new HashMap<>();
        orderToDraw = new ArrayList<>();

        addRenderLayer("Background", 0);
        addRenderLayer("Objects", 10);
        addRenderLayer("UI", 100);
    }

    /**
     * Creates a new Camera object with the default scale.
     * <p>
     * This process includes adding the default layers.
     * @param x starting X position of the camera.
     * @param y starting Y position of the camera.
     */
    public Camera(double x, double y){
        this(x, y, 1);
    }

    /**
     * Creates a new Camera object with the default starting position and scale.
     * <p>
     * This process includes adding the default layers.
     */
    public Camera() {
        this(0,0);
    }

    /**
     * Adds a new rendering layer to the camera to add objects to using {@link #addObjectToLayer(String, GameObject)}.
     * <p>
     * The camera automatically has default layers: Background (depth 0), Objects (depth 10), and UI (depth 100) set
     * upon instantiation.
     * <p>
     * <b>How depth works:</b>
     * <ul>
     * <li>Depth can be any real integer, including negative ones.</li>
     * <li>A layer's depth determines its rendering order relative to other layers.</li>
     * <li>Higher depth values result in layers being rendered on top of lower depth layers.</li>
     * <li>For example, if Layer A has a higher depth than Layer B, Layer A will be rendered on top of Layer B.</li>
     * <li>If Layer C has a higher depth than both Layer A and Layer B, it will be rendered on top of both.</li>
     * </ul>
     * 
     * <p>
     * 
     * Keep in mind objects on layers are drawn in order of when they were added, oldest first.
     * 
     * @param name the name of the layer being created
     * @param depth the depth of the layer being created
     * @throws IllegalArgumentException if a layer with the exact same depth already exists
     */
    public void addRenderLayer(String name, int depth) throws IllegalArgumentException {
        for(String key : layerDepths.keySet()) {
            if(layerDepths.get(key) == depth) throw new IllegalArgumentException("A layer with depth " + depth + " already exists: " + key);
        }
        layers.put(name, new ArrayList<GameObject>());
        layerDepths.put(name, depth);

        // clear the order, we're remaking it! yay!
        // todo: maybe just figure out how to insert it?? instead of redoing the whole thing
        orderToDraw.clear();
        // sort the list so we can order them from lowest depth to highest
        ArrayList<Integer> vals = new ArrayList<>();
        // i dont think theres already a function for this
        for(String key : layerDepths.keySet()) {
            vals.add(layerDepths.get(key));
        }
        // this is why we need to bank on only one layer of each depth existing
        Collections.sort(vals);
        for(Integer val : vals) {
            orderToDraw.add(getKeyFromDepth(val));
        }
        //System.out.println(orderToDraw);
    }

    /**
     * Adds a GameObject or any of its subclasses to a render layer to be drawn to the screen.
     * @param layer layer to add the object to
     * @param object object to be added to the layer
     * @throws IllegalArgumentException if an invalid layer name is used
     */
    public void addObjectToLayer(String layer, GameObject object) throws IllegalArgumentException {
        if (!layers.keySet().contains(layer)) {
            throw new IllegalArgumentException("No layer with the name '" + layer + "' exists!");
        }
        object.drawID = layers.get(layer).size();
        layers.get(layer).add(object);
    }

    public void removeObjectFromLayer(String layer, GameObject object) throws IllegalArgumentException {
        if (!layers.keySet().contains(layer)) {
            throw new IllegalArgumentException("No layer with the name '" + layer + "' exists!");
        }

        boolean foundObject = false;
        GameObject target = null;
        for(GameObject obj : layers.get(layer)){
            if (obj.drawID == object.drawID) {
                foundObject = true;
                target = obj;
                break;
            } 
        }
        if(!foundObject) {
            //System.out.println("COULD NOT FIND OBJECT WITH DRAW ID " + object.drawID);
        }
        else layers.get(layer).remove(target);
    }

    public int getLayerDepth(String layer) throws IllegalArgumentException {
        if (!layers.keySet().contains(layer)) {
            throw new IllegalArgumentException("No layer with the name '" + layer + "' exists!");
        }

        return layerDepths.get(layer);
    }
    // camera movement methods

    /**
     * Moves the camera the specified amount on the X axis and recalculates its bounds.
     * @param x amount to move on the X axis
     */
    public void moveCameraX(double x) {
        this.x += x;
        recalculateBounds();
    }

    /**
     * Moves the camera the specified amount on the Y axis and recalculates its bounds.
     * @param y amount to move on the Y axis
     */
    public void moveCameraY(double y) {
        this.y += y;
        recalculateBounds();
    }

    /**
     * Moves the camera the specified amounts on the X and Y axis, respectively, and recalculates its bounds.
     * @param x amount to move on the X axis
     * @param y amount to move on the Y axis
     */
    public void moveCamera(double x, double y) {
        this.x += x;
        this.y += y;
        recalculateBounds();
    }

    /**
     * Sets the camera's position to the specified X and Y positions and recalculates its bounds.
     * @param x position on the X axis to set the camera's to
     * @param y position on the Y axis to set the camera's to
     */
    public void setCameraPos(double x, double y) {
        this.x = x;
        this.y = y;
        recalculateBounds();
    }

    /**
     * Sets the camera's background to the color specified.
     * <p>
     * The background is a color or image drawn before any other object assigned to a layer.
     * @param newBG color to set the background to
     */
    public void setBackground(Color newBG){
        background = newBG;
        bgBI = new BufferedImage(Main.windowWidth, Main.windowHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bgGraphics = bgBI.createGraphics();
        bgGraphics.setColor(background);
        bgGraphics.fillRect(0, 0, bgBI.getWidth(), bgBI.getHeight());
        bgGraphics.dispose();
    }

    /**
     * Sets the camera's background to the image specified.
     * <p>
     * The background is a color or image drawn before any other object assigned to a layer.
     * @param newBG image to set the background to
     */
    public void setBackground(BufferedImage newBG){
        background = null;
        bgBI = newBG;
    }

    /**
     * Get an array of the background color (null if background is an image) and background image of the camera
     * <p>
     * Both of these will be null if no background is set
     * @return background color (if applicable) and image
     */
    public Object[] getBackground(){
        Object[] ret = {background, bgBI};
        return ret;
    }

    /**
     * Zooms the camera in or out based on the inputted value. Also recalculates its bounds.
     * @param zoom amount to zoom in
     */
    public void changeCameraZoom(double zoom) {
        scaleFactor += zoom;
        if(scaleFactor < 0.05) scaleFactor = 1;
        recalculateBounds();
    }

    /**
     * Sets the camera's scale (zoom) to the inputted value. Also recalculates its bounds.
     * @param scale scale to set the camera to
     */
    public void setCameraScale(double scale) {
        scaleFactor = scale;
        recalculateBounds();
    }

    private String getKeyFromDepth(int depth) {
        for(String key : layerDepths.keySet()) {
            if(layerDepths.get(key) == depth) return key;
        }
        return null; // no key has been found
    }

    private void recalculateBounds(){
        bounds.setRect(x, y, Main.windowWidth, Main.windowHeight);
    }

    public Rectangle getBounds(){
        return bounds;
    }

    /**
     * Draws all current layers in order to the Graphics2D object supplied.
     * @param g Graphics2D object to draw to.
     */
    public void drawViewport(Graphics2D g) {
        // int drawn = 0;
        if(bgBI != null) g.drawImage(bgBI, 0, 0, null);
        for(String key : orderToDraw) {
            for(GameObject object : layers.get(key)) {
                if(true) { // TODO: check to make sure the camera can actually see it lol
                    object.render(g, this);
                    // drawn++;
                }
            }
        }
        // System.out.println("Objects drawn: " + drawn);
    }
}
