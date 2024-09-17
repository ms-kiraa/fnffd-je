import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua51.Lua51;

import party.iroiro.luajava.value.LuaValue;
import party.iroiro.luajava.value.LuaFunction;

public class FreeDownloadLua {

    public Lua51[] luaScripts;
    private SongData songData;

    public FreeDownloadLua(SongData song){
        this.songData = song;
        File[] scripts = findLuaScripts();
        luaScripts = new Lua51[scripts.length];
        for(int i = 0; i < luaScripts.length; i++){
            luaScripts[i] = new Lua51();
            try{
                luaScripts[i].run(Files.readString(Paths.get(scripts[i].getAbsolutePath())));
            } catch(Exception e){
                System.err.println("FAILED TO INIT LUA SCRIPT!");
                e.printStackTrace();
            }
        }
        setupGlobals();
    }

    private void setupGlobals(){
        for(Lua51 script : luaScripts){
            // open global libraries
            script.openLibrary("math");
            script.openLibrary("table");

            // just plain variables
            // song names
            script.set("songName", songData.formalName);
            script.set("songFileName", songData.fileName);

            // character names
            script.set("dudeChar", songData.dudeChar);
            script.set("badguyChar", songData.badguyChar);
            script.set("ladyChar", songData.ladyChar);

            // settings
            script.set("downscroll", ClientPrefs.getDownscroll());

            // GOod
            script.set("hawk", "tuah");

            // getters
            // positions
            script.set("getDudeX", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    System.out.println(Stage.instance.dude.x);
                    return script.eval("return "+Stage.instance.dude.x);
                } 
            });
            script.set("getDudeY", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Stage.instance.dude.y);
                } 
            });

            script.set("getBadguyX", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Stage.instance.badguy.x);
                } 
            });
            script.set("getBadguyY", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Stage.instance.badguy.y);
                } 
            });

            script.set("getNoteX", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0=dude/badguy, 1=note 0-3
                    return script.eval("return "+Stage.instance.uiNotes.get(args[0].toString()).get((int)args[1].toInteger()).x);
                } 
            });
            script.set("getNoteY", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0=dude/badguy, 1=note 0-3
                    return script.eval("return "+Stage.instance.uiNotes.get(args[0].toString()).get((int)args[1].toInteger()).y);
                } 
            });

            // time stuff
            script.set("getStep", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Conductor.getStep());
                }
            });
            script.set("getStepDb", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Conductor.getStepDb());
                }
            });
            script.set("getBeat", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Conductor.getBeat());
                }
            });
            script.set("getBeatDb", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Conductor.getBeatDb());
                }
            });
            script.set("getSongPosition", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    return script.eval("return "+Conductor.getTime());
                }
            });

            // cam
            script.set("getCamTarget", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    return script.eval("return {" + Stage.instance.camXTarget+","+Stage.instance.camXTarget+"}");
                }
            });
            script.set("getCamPos", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    return script.eval("return {" + Stage.instance.cam.x+","+Stage.instance.cam.y+"}");
                }
            });

            // setters
            // positions
            script.set("setDudePosition", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    //System.out.println(args[0].toNumber());
                    Stage.instance.dude.setPosition(args[0].toNumber(), args[1].toNumber());
                    return null;
                }
            });
            script.set("setBadguyPosition", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    Stage.instance.badguy.setPosition(args[0].toNumber(), args[1].toNumber());
                    return null;
                }
            });
            script.set("setNotePosition", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0="dude"/"badguy", 1=note num, 0-3, 2=x, 3=y
                    UINote uin = Stage.instance.uiNotes.get(args[0].toString()).get((int)args[1].toInteger());
                    uin.setPosition(args[2].toNumber(), args[3].toNumber());
                    int idx = (int)args[1].toInteger() + (args[0].toString().equals("Player") ? 4 : 0);
                    Iterator<GameNote> it = Stage.instance.curChart.get(idx).iterator();
                    while(it.hasNext()){
                        GameNote gn = it.next();

                        gn.x = args[2].toNumber();
                        gn.yy = gn.actualYY + uin.y-Stage.instance.uiNotesYPos;
                    }
                    return null;
                }
            });
            
            // cam
            script.set("setCamTarget", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    Stage.instance.camXTarget = args[0].toNumber();
                    Stage.instance.camYTarget = args[1].toNumber();
                    return null;
                }
            });
            script.set("setCamPos", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    Stage.instance.cam.x = args[0].toNumber();
                    Stage.instance.cam.y = args[1].toNumber();
                    return null;
                }
            });
            script.set("setCamZoom", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    Stage.instance.cam.scaleFactor = args[0].toNumber();
                    return null;
                }
            });

            // util for color
            script.set("rgbToHex", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    return script.eval("return " + String.format("#%02x%02x%02x", args[0].toInteger(), args[1].toInteger(), args[2].toInteger()));
                }
            });

            script.set("camFlash", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0 = time between tick, 1 = color
                    FadeManager.cancelFade();
                    FadeManager.fadeIn(Color.decode(args[1].toString()), 1, (int)args[0].toInteger());
                    System.out.println("flashing");
                    return null;
                }
            });
            script.set("camFade", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0 = time between tick, 1 = color
                    FadeManager.cancelFade();
                    FadeManager.fadeOut(Color.decode(args[1].toString()), 1, (int)args[0].toInteger());
                    System.out.println("fading");
                    return null;
                }
            });
            script.set("cancelFade", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    FadeManager.cancelFade();
                    return null;
                }
            });

            // copes to deal with no setProperty
            script.set("setNoteVisibility", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    for(LuaValue arg : args){
                        System.out.println(arg.toString());
                    }
                    UINote uin = Stage.instance.uiNotes.get(args[0].toString()).get((int)args[1].toInteger());
                    uin.visible = Boolean.parseBoolean(args[2].toString());
                    int idx = (int)args[1].toInteger() + (args[0].toString().equals("Player") ? 4 : 0);
                    Iterator<GameNote> it = Stage.instance.curChart.get(idx).iterator();
                    while(it.hasNext()){
                        GameNote gn = it.next();

                        gn.visible = Boolean.parseBoolean(args[2].toString());
                    }
                    return null;
                }
            });

            // misc
            script.set("playSFX", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    SoundManager.playSFX("./snd/"+args[0].toString()+".wav");
                    return null;
                }
            });
        }
    }

    public void updateGlobalValue(String name, Object value){
        for(Lua51 script : luaScripts){
            script.set(name, value);
        }
    }

    public File[] findLuaScripts(){
        ArrayList<File> luas = new ArrayList<>();

        for(File f : songData.folder.listFiles()){
            if(f.getName().endsWith(".lua")){
                System.out.println(f.getName());
                luas.add(f);
            }
        }
        return luas.toArray(new File[0]);
    }

    public void fireLuaFunction(String function, Object... args){
        try{
            for(Lua51 luaInstance : luaScripts) {
                LuaValue func = luaInstance.eval("return "+function)[0];
                if(func.toString() != "nil"){
                    func.call(args);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.print(function + " errored with args '");
            for(Object arg : args){
                System.out.print(arg);
            }
            System.out.println("'");
        }
    }
}
