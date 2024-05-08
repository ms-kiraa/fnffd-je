import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Camera {
    private Map<String, List<GameObject>> layers;
    private Map<String, Integer> layerDepths;

    public double x;
    public double y;

    public double scaleFactor;

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

        layers = new HashMap<>();
        layerDepths = new HashMap<>();

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
        this(x, y, 1.5);
    }

    /**
     * Creates a new Camera object with the default starting position and scale.
     * <p>
     * This process includes adding the default layers.
     */
    public Camera() {
        this(0,0,1.5);
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
     */
    public void addRenderLayer(String name, int depth) {
        layers.put(name, new ArrayList<GameObject>());
        layerDepths.put(name, depth);
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

        layers.get(layer).add(object);
    }

    /**
     * Draws all current layers in order to the Graphics2D object supplied.
     * @param g Graphics2D object to draw to.
     */
    public void drawViewport(Graphics2D g) {
        
    }
}
