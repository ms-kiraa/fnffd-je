package effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.imagemanip.ColorMapper;
import backend.imagemanip.ImageEffect;

public class ColorReplaceEffect implements ImageEffect {

    Map<List<Integer>, List<Integer>> findAndReplace = new HashMap<>();

    public ColorReplaceEffect(List<List<Integer>> from, List<List<Integer>> to){
        for(int i = 0; i < from.size(); i++){
            findAndReplace.put(from.get(i), to.get(i));
        }
    }

    // this is dumb
    public ColorReplaceEffect(ArrayList<ArrayList<Integer>> from, ArrayList<ArrayList<Integer>> to) {
        for(int i = 0; i < from.size(); i++){
            findAndReplace.put(from.get(i), to.get(i));
        }
    }

    private BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    @Override
    public BufferedImage apply(BufferedImage bi) {
        BufferedImage ret = copyImage(bi);
        for(List<Integer> fromRGB : findAndReplace.keySet()){
            Color from = new Color(fromRGB.get(0), fromRGB.get(1), fromRGB.get(2));
            List<Integer> toRGB = findAndReplace.get(fromRGB);
            Color to = new Color(toRGB.get(0), toRGB.get(1), toRGB.get(2));
            BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
            ret = lookup.filter(ret, null);
        }
        return ret;
    }

    @Override
    public BufferedImage remove(BufferedImage bi) {
        BufferedImage ret = copyImage(bi);
        for(List<Integer> fromRGB : findAndReplace.keySet()){
            List<Integer> toRGB = findAndReplace.get(fromRGB);
            Color from = new Color(toRGB.get(0), toRGB.get(1), toRGB.get(2));
            Color to = new Color(fromRGB.get(0), fromRGB.get(1), fromRGB.get(2));
            BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
            ret = lookup.filter(ret, null);
        }
        return ret;
    }

}

