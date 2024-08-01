import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class GameNote extends Note {
    public NoteType type;
    public boolean shouldDraw = true;
    public boolean drawCap = false;
    private double yy;

    public GameNote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote, NoteType type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = type;
        this.yy = y;
        setImage();
    }

    public GameNote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote, NoteType type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = type;
        this.yy = y;
        setImage();
    }

    public GameNote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote, int type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = NoteType.convertIntToNoteType(type);
        this.yy = y;
        setImage();
    }

    public GameNote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote, int type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = NoteType.convertIntToNoteType(type);
        this.yy = y;
        setImage();
    }

    private void setImage(){
        String basePath = "./img/ui/notes/";
        switch(type){
            case BOMB:
                basePath += "bombs/spr_bombs_";
                break;
            case DUDE_CAM:
            case ENEMY_CAM:
            case MIDDLE_CAM:
            case AYY:
                shouldDraw = false;
                break;
            case HOLD:
            case ALT_HOLD:
                basePath += "spr_noteshold_";
                drawCap = true; // TODO: make it only draw the cap if its the last in line please pretty please remember to do it this time dont be like last time
                break;
            case EVENT:
                basePath += "event/spr_eventnote";
                break;
            default:
                if(!fumpNote){
                    basePath += "spr_notes_";
                } else {
                    basePath += "fump/spr_notefump_";
                }
                break;
        }
        if(shouldDraw){
            try{
                //System.out.println(basePath);
                this.image = ImageIO.read(new File(basePath + dir.getDirectionAsInt() + ".png"));
            } catch(Exception e){
                e.printStackTrace();
                //System.exit(0);
                shouldDraw = false;
            }
        }
    }

    @Override
    public void render(Graphics2D g, Camera cam) {
        if(shouldDraw){
            super.render(g, cam);
        }
        if(drawCap){
            // todo
        }
        update();
    }

    @Override
    protected void update(){
    }

    public void move(double amt){
        double newY = yy - amt;
        y = newY;
    }
    
    public enum NoteType {
        NORMAL, ALT, BOMB, DUDE_CAM, ENEMY_CAM, MIDDLE_CAM, AYY, HOLD, ALT_HOLD, EVENT;

        public int toInt(){
            switch(this){
                case ALT:
                    return 2;
                case BOMB:
                    return 3;
                case DUDE_CAM:
                    return 4;
                case ENEMY_CAM:
                    return 5;
                case MIDDLE_CAM:
                    return 6;
                case AYY:
                    return 7;
                case HOLD:
                    return 8;
                case ALT_HOLD:
                    return 9;
                case EVENT:
                    return 10;
                default:
                    return 1;
            }
        }

        public static NoteType convertIntToNoteType(int typeInt){
            /*
             * LEGEND:
             * 1 = normal note
             * 2 = alt anim
             * 3 = bomb
             * 4 = dudecam
             * 5 = enemy cam
             * 6 = both
             * 7 = ayy
             * 8 = hold
             * 9 = alt hold (?)
             * 10 = event
             */
            switch(typeInt){
                case 1:
                    return NORMAL;
                case 2:
                    return ALT;
                case 3:
                    return BOMB;
                case 4:
                    return DUDE_CAM;
                case 5:
                    return ENEMY_CAM;
                case 6:
                    return MIDDLE_CAM;
                case 7:
                    return AYY;
                case 8:
                    return HOLD;
                case 9:
                    return ALT_HOLD;
                case 10:
                    return EVENT;
    
            }
            return NORMAL;
        }   
    }
}
