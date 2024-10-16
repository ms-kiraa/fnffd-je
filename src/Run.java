import javax.swing.SwingUtilities;

import main.Main;

// because java packaging sucks and it cant find the main method if it's in a folder and other classes cant see something if its not in a folder
public class Run {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {Main.main = new Main("fnf free download: java edition");});
    }
}
