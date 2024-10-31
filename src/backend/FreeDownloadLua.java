package backend;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import backend.data.SongData;
import backend.imagemanip.ImageUtils;
import backend.managers.*;
import backend.save.ClientPrefs;
import effects.ColorReplaceEffect;
import effects.DudeSkins;
import main.Main;
import notes.GameNote;
import notes.UINote;
import objects.AnimatedGameObject;
import objects.fx.FXAnimatedGameObject;
import objects.fx.FXGameObject;
import panels.Stage;

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
        for(int i = 0; i < luaScripts.length; i++){
            try{
                luaScripts[i].run(Files.readString(Paths.get(scripts[i].getAbsolutePath())));
            } catch(Exception e){
                System.err.println("FAILED TO INIT LUA SCRIPT!");
                e.printStackTrace();
            }
        }
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

            // window info
            script.set("screenWidth", Main.windowWidth);
            script.set("screenHeight", Main.windowHeight);

            // settings
            script.set("downscroll", ClientPrefs.getBoolean("downscroll"));

            // GOod
            script.set("hawk", "tuah");

            // getters
            // positions
            script.set("getDudeX", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] arg1) {
                    //System.out.println(Stage.instance.dude.x);
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
                    //System.out.println("flashing");
                    return null;
                }
            });
            script.set("camFade", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    // args: 0 = time between tick, 1 = color
                    FadeManager.cancelFade();
                    FadeManager.fadeOut(Color.decode(args[1].toString()), 1, (int)args[0].toInteger());
                    //System.out.println("fading");
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
                    /*for(LuaValue arg : args){
                        //System.out.println(arg.toString());
                    }*/
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

            script.set("setBackground", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    Stage.instance.cam.setBackground(new Color((int)args[0].toInteger(), (int)args[1].toInteger(), (int)args[2].toInteger(), 255));
                    return null;
                }
            });

            // this is better than setproperty cause it adjusts their origin to be their feet rather than the top of their head
            script.set("setCharacterPosition", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    AnimatedGameObject ref = null;
                    boolean leftRight = false;
                    switch(args[0].toString().toLowerCase()) {
                        case "dude":
                            ref = Stage.instance.dude;
                            leftRight = Stage.instance.dudeLeftRightIdleStyle;
                            break;
                        case "badguy":
                            ref = Stage.instance.badguy;
                            leftRight = Stage.instance.badguyLeftRightIdleStyle;
                            break;
                        // lady support coming eventually
                    }

                    int height = 0;
                    if(leftRight) {
                        height = ref.animations.get("idle-left").get(0).getHeight();
                    } else {
                        height = ref.animations.get("idle").get(0).getHeight();
                    }
                    ref.setPosition(args[1].toNumber(), args[2].toNumber()-height);
                    return null;
                }
            });

            // about that!

            // i have never ever done reflection
            // this is gross but whatever
            script.set("setProperty", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    try{
                        Field ref = Stage.class.getDeclaredField(args[0].toString());
                        ref.setAccessible(true);
                        ref.set(Stage.instance, args[1].toJavaObject());
                        ref.setAccessible(false);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            });

            script.set("setPropertyOfObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    try {
                        // Step 1: Find the field in the class hierarchy (handles inherited fields)
                        Field ref = ReflectionUtils.getFieldFromClassHierarchy(Stage.class, args[0].toString());
                        ref.setAccessible(true);
                    
                        // Step 2: Get the object from Stage.instance
                        Object parentObject = ref.get(Stage.instance);
                    
                        // Step 3: Find the sub-field in the hierarchy of the parent object
                        Field subref = ReflectionUtils.getFieldFromClassHierarchy(parentObject.getClass(), args[1].toString());
                        subref.setAccessible(true);
                    
                        // Step 4: Set the sub-field value
                        subref.set(parentObject, args[2].toJavaObject());
                    
                        // Cleanup (optional)
                        subref.setAccessible(false);
                        ref.setAccessible(false);
                    
                    } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });

            // this cant return any arrays or anything
            script.set("getProperty", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    try{
                        Field ref = Stage.class.getDeclaredField(args[0].toString());
                        ref.setAccessible(true);
                        Object gotten = ref.get(Stage.instance);
                        LuaValue[] val = script.eval("return \"" + gotten + "\""); // this is complete and utter bullshit dude
                        return val;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("whoops");
                    return null;
                }
            });

            // stage manip methods
            script.set("makeLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    String path = "./stages/"+Stage.instance.songData.stage + "/" + args[1].toString();
                    double x = args[2].toNumber();
                    double y = args[3].toNumber();
                    System.out.println(path);
                    try{
                        FXGameObject obj = new FXGameObject(x, y, 1, FileRetriever.image(path), Stage.instance.cam);
                        Stage.instance.stageObjects.put(objName, obj);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            });
            script.set("makeSolidLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    String color = args[1].toString();
                    int width = (int) args[2].toInteger();
                    int height = (int) args[3].toInteger();
                    double x = args[4].toNumber();
                    double y = args[5].toNumber();

                    FXGameObject obj = new FXGameObject(x, y, 1, ImageUtils.makeSolid(width, height, Color.decode(color)), Stage.instance.cam);
                    Stage.instance.stageObjects.put(objName, obj);

                    return null;
                }
            });

            script.set("addLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    String layer = "Background";
                    if(args.length > 1) { 
                        layer = args[1].toString();
                    }
                    Stage.instance.cam.addObjectToLayer(layer, Stage.instance.stageObjects.get(objName));
                    Stage.instance.objectLayers.put(objName, layer);

                    return null;
                }
            });
            script.set("removeLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    String layer = Stage.instance.objectLayers.get(objName);

                    FXGameObject obj = Stage.instance.stageObjects.get(objName);
                    Stage.instance.cam.removeObjectFromLayer(layer, obj);

                    return null;
                }
            });
            script.set("setLuaObjectLayer", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    String layer = Stage.instance.objectLayers.get(objName);

                    Stage.instance.cam.removeObjectFromLayer(layer, Stage.instance.stageObjects.get(objName));

                    String newLayer = args[1].toString();

                    Stage.instance.cam.addObjectToLayer(newLayer, Stage.instance.stageObjects.get(objName));
                    Stage.instance.objectLayers.put(objName, newLayer);

                    return null;
                }
            });

            script.set("setLuaObjectScrollFactor", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    
                    Stage.instance.stageObjects.get(objName).scrollFactor = args[1].toNumber();

                    return null;
                }
            });

            script.set("scaleLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    String objName = args[0].toString();
                    
                    Stage.instance.stageObjects.get(objName).scale = args[1].toNumber();

                    return null;
                }
            });

            // this is stupid but must be done
            script.set("applyDudeSkinToObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    FXGameObject obj = Stage.instance.stageObjects.get(args[0].toString());
                    obj.addEffect(ClientPrefs.dudeSkin.skin);
                    if(ClientPrefs.dudeSkin == DudeSkins.Kira) {
                        // hi again mika
                        obj.addEffect(new ColorReplaceEffect(
                            Arrays.asList(
                                Arrays.asList(198,192,179),
                                Arrays.asList(88,61,95),
                                Arrays.asList(63,114,112),
                                Arrays.asList(187,201,208),
                                Arrays.asList(53,69,77)
                            ),
                            Arrays.asList(
                                Arrays.asList(88, 68, 48), // hair
                                Arrays.asList(245, 47, 106), // streak
                                Arrays.asList(224, 83, 109), // dress
                                Arrays.asList(209, 119, 150), // socks
                                Arrays.asList(110, 48, 89) // shoes
                            )));
                    }
                    
                    return null;
                }
            });

            // this doesnt workkkkkk
            script.set("centerLuaObject", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    FXGameObject obj = Stage.instance.stageObjects.get(args[0].toString());
                    obj.drawHitbox = true;
                    obj.setPosition(
                        Stage.instance.getWidth()/2-(obj.image.getWidth()*obj.scale*obj.cam.scaleFactor)/2, 
                        Stage.instance.getHeight()/2-(obj.image.getHeight()*obj.scale*obj.cam.scaleFactor)/2
                    );
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





            // im not proud of this
            script.set("makeImage", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    BufferedImage im = null;
                    im = FileRetriever.image("stages/"+Stage.instance.songData.stage + "/" + args[1].toString());
                    Stage.instance.stageImages.put(args[0].toString(), im);
                    return null;
                }
            });

            script.set("applyDudeSkinToImage", new LuaFunction() {
                @Override
                public LuaValue[] call(Lua arg0, LuaValue[] args) {
                    BufferedImage image = Stage.instance.stageImages.get(args[0].toString());
                    image = ClientPrefs.dudeSkin.skin.apply(image);
                    if(ClientPrefs.dudeSkin == DudeSkins.Kira) {
                        // hi again mika (this is done EXCLUSIVELY for starfire LOL)
                        image = (new ColorReplaceEffect(
                            Arrays.asList(
                                Arrays.asList(198,192,179),
                                Arrays.asList(88,61,95),
                                Arrays.asList(63,114,112),
                                Arrays.asList(187,201,208),
                                Arrays.asList(53,69,77)
                            ),
                            Arrays.asList(
                                Arrays.asList(88, 68, 48), // hair
                                Arrays.asList(245, 47, 106), // streak
                                Arrays.asList(224, 83, 109), // dress
                                Arrays.asList(209, 119, 150), // socks
                                Arrays.asList(110, 48, 89) // shoes
                            ))).apply(image);
                    }
                    Stage.instance.stageImages.put(args[0].toString(), image);
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
                System.out.println("Found Lua script " + f.getName());
                luas.add(f);
            }
        }
        for(File f : new File("./stages/"+songData.stage).listFiles()) {
            if(f.getName().endsWith(".lua")){
                System.out.println("Found Lua stage file " + f.getName());
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
            System.out.print(function + " errored" + (args.length > 0 ?  " with args '" : ""));
            if(args.length > 0) {
                for(Object arg : args){
                    System.out.print(arg);
                }
                System.out.println("'");
            }
        }
    }
}
