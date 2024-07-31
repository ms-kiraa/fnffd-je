public class GameNote {

    
    public enum NoteType {
        NORMAL, ALT, BOMB, DUDE_CAM, ENEMY_CAM, MIDDLE_CAM, AYY, HOLD, ALT_HOLD, EVENT;

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
