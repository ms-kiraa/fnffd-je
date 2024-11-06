package notes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import backend.Camera;
import backend.save.ClientPrefs;
import panels.Stage;

public class GameNote extends Note {
    public NoteType type;
    public boolean shouldDraw = true;
    public boolean drawCap = false;
    public double actualYY;
    public double yy;
    public boolean autohit = false;
    public boolean hit = false;

    public double timeMS = -1;

    private boolean debug = ClientPrefs.getBoolean("note_debug");

    public GameNote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote, NoteType type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = type;
        this.yy = y;
        this.actualYY = y;
        setImage();
    }

    public GameNote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote, NoteType type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = type;
        this.yy = y;
        this.actualYY = y;
        setImage();
    }

    public GameNote(double x, double y, Direction direction, Camera camera, boolean fump, boolean playerNote, int type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = NoteType.convertIntToNoteType(type);
        this.yy = y;
        this.actualYY = y;
        setImage();
    }

    public GameNote(double x, double y, int direction, Camera camera, boolean fump, boolean playerNote, int type){
        super(x, y, direction, camera, fump, playerNote);
        this.type = NoteType.convertIntToNoteType(type);
        this.yy = y;
        this.actualYY = y;
        setImage();
    }

    private void setImage(){
        String basePath = "./img/ui/notes/";
        switch(type){
            case BOMB:
                basePath += "bombs/";
                switch(Stage.instance.songName){
                    case "mus_w3s2":
                        //System.out.println("Tsunami");
                        if(playerNote) {
                            basePath += "buddy/spr_notes3_";
                            autohit = true;
                        }
                        else basePath += "spr_bombs_";
                        break;
                    case "mus_channelsurf":
                        //System.out.println("Channelsurfing");
                        basePath += "nermal/spr_bombsn_";
                        break;
                    case "mus_w4s1":
                        if(!playerNote){
                            basePath = "./img/ui/notes/";
                            if(!fumpNote){
                                basePath += "spr_notes_";
                            } else {
                                basePath += "fump/spr_notefump_";
                            }
                        } else {
                            basePath += "spr_bombs_";
                        }
                        break;
                    default:
                        basePath += "spr_bombs_";
                        break;
                }
                break;
            case DUDE_CAM:
            case ENEMY_CAM:
            case MIDDLE_CAM:
            case AYY:
                basePath += "fump/spr_notefump_";
                if(!debug) shouldDraw = false;
                else alpha = 0.5f;
                autohit = true;
                break;
            case HOLD:
            case ALT_HOLD:
                basePath += "spr_noteshold_";
                break;
            case EVENT:
                basePath += "event/spr_eventnote";
                if(!debug) shouldDraw = false;
                else alpha = 0.5f;
                autohit = true;
                break;
            case END_SONG_TRIGGER:
                basePath += "spr_noteshold_";
                autohit = true;
                if(!debug) shouldDraw = false;
                else alpha = 0.5f;
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
            String path = basePath + dir.getDirectionAsInt() + ".png";
            BufferedImage img = getImageFromCache(path);
            setImage(img);
        }
    }

    public void enableCap(){
        drawCap = true;
        String path = "./img/ui/notes/spr_notecap_"+dir.getDirectionAsInt()+".png";
        if(Stage.instance.downscroll) yy -= this.image.getHeight(); // move it up so that it appears in the right place
        //if(Stage.instance.downscroll) yy -= (getImageFromCache(path).getHeight()/3-5);
        if(!cache.containsKey(dir.getDirectionAsInt()+"cap")){
            // edit the image ot have the cap under it. im lazy
            BufferedImage hold = this.image;
            BufferedImage cap = getImageFromCache(path);
            BufferedImage merged = new BufferedImage(hold.getWidth(), hold.getHeight()+cap.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = merged.createGraphics();
            if(!Stage.instance.downscroll){
                g2.drawImage(cap, 0,merged.getHeight()-cap.getHeight(), null);
                g2.drawImage(hold, 0, 0, null);
            } else {
                // FIXME: this makes it so you have to hold the note for longer
                g2.drawImage(hold, 0, merged.getHeight()-hold.getHeight(), null);
                g2.drawImage(cap, 0, cap.getHeight(), cap.getWidth(), -cap.getHeight(), null);
                /*// HACK: this is a short-term solution for a problem that really should be fixed in the long run
                g2.dispose();
                merged = new BufferedImage(cap.getWidth(), cap.getHeight(), BufferedImage.TYPE_INT_ARGB);
                g2 = merged.createGraphics();
                g2.drawImage(cap, 0, merged.getHeight(), cap.getWidth(), -cap.getHeight(), null);
                yy -= (merged.getHeight()/3-5);*/
            }
            g2.dispose();
            cache.put(dir.getDirectionAsInt()+"cap", merged);
        }
        setImage(cache.get(dir.getDirectionAsInt()+"cap"));
    }

    @Override
    public void render(Graphics2D g, Camera cam) {
        if(shouldDraw){
            super.render(g, cam);
        }
    }

    public boolean isHold(){
        return (type == NoteType.HOLD || type == NoteType.ALT_HOLD);
    }

    @Override
    protected void update(){
    }

    public void move(double amt){
        double newY = yy - amt;
        if(newY <= 800 && newY >= 0-image.getHeight()*5){
            y = newY;
        }
    }

    public boolean sameAs(GameNote obj){
        if(obj.x == x && obj.yy == yy) return true;
        return false;
    }
    
    public enum NoteType {
        NORMAL, ALT, BOMB, DUDE_CAM, ENEMY_CAM, MIDDLE_CAM, AYY, HOLD, ALT_HOLD, EVENT, END_SONG_TRIGGER; // special note used exclusively to end the song

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
