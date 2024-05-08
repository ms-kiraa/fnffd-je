/**
 * The base object of which all other stage objects are derived. Pretty useless and does nothing on its own but can be extended for further functionality.
 */

public class GameObject {

    public double x;
    public double y;

    public GameObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

}
