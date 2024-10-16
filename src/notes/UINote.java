package notes;

import backend.Camera;

public class UINote extends Note {
    
    public static int presses = 0;

    public UINote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, direction, camera, fump, playerNote);
        this.playerNote = playerNote;
        setImage();
    }

    public UINote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote){
        super(x, y, direction, camera, fump, playerNote);
        this.playerNote = playerNote;
        setImage();
    }

    private void setImage(){
        String path = "./img/ui/notes/ui/" + ((fumpNote) ? ("fump/spr_uinotefump_") : ("spr_uinotes_"));
        path += this.dir.getDirectionAsInt() + ".png";
        setImage(getImageFromCache(path));
    }

    public void visPress(){
        presses++;
        alpha = 0.3f;
        String threadName = (playerNote ? "Player" : "BadGuy") + this.dir.getDirectionAsString(CapsMode.UPPER_CAMEL_CASE) + "PressThread" + presses;
        Thread pressThread = new Thread(()->{
            try{Thread.sleep(200);}catch(Exception e){e.printStackTrace();}
            alpha = 1f;
        });
        pressThread.setName(threadName);
        pressThread.start();
    }

    /*
     * commented out cause i redid how alpha works for gameobjects lul
     */
    /*@Override
    public void render(Graphics2D g, Camera cam) {
        AlphaComposite oldAC = (AlphaComposite) g.getComposite();
        if(pressed){
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g.setComposite(ac);
        }
        super.render(g, cam);
        if(pressed) g.setComposite(oldAC);
    }*/
}
