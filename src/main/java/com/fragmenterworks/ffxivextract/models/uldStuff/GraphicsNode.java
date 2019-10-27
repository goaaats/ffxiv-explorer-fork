package com.fragmenterworks.ffxivextract.models.uldStuff;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.ULD_File;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class GraphicsNode {

    public final int index;            //UNSIGNED_LONG_PACK
    public final int parent;            //UNSIGNED_LONG_PACK
    public final int previous;            //UNSIGNED_LONG_PACK
    public final int next;            //UNSIGNED_LONG_PACK
    public final int last;            //UNSIGNED_LONG_PACK
    public final int type;            //UNSIGNED_LONG_PACK @ 0x14
    private final int size;            //UNSIGNED_LONG_PACK @ 0x24
    public final int x;            //SIGNED_SHORT_PACK @ 0x2c
    public final int y;            //SIGNED_SHORT_PACK @
    public final int w;            //SIGNED_SHORT_PACK @
    public final int h;            //SIGNED_SHORT_PACK @
    public final float rotation;            //FLOAT_PACK @
    public final float scaleX;            //FLOAT_PACK @ 0x38
    public final float scaleY;            //FLOAT_PACK @ 0x3c
    public final int xOrigin;            //SIGNED_SHORT_PACK @ 0x40
    public final int yOrigin;            //SIGNED_SHORT_PACK @ 0x42
    private final int unknown_46;            //SIGNED_SHORT_PACK @ 0x46
    private final int unknown_48;            //SIGNED_SHORT_PACK @ 0x48
    private final int unknown_4a;            //SIGNED_SHORT_PACK @ 0x4a
    private final int unknown_4c;            //SIGNED_SHORT_PACK @ 0x4c
    private final int word_4e;            //SIGNED_SHORT_PACK @ 0x4e
    private final int word_50;            //SIGNED_SHORT_PACK @ 0x50
    private final int unknown_54;            //SIGNED_SHORT_PACK @ 0x54
    public final int layer;            //SIGNED_SHORT_PACK @ 0x56

    public final GraphicsNodeTypeData typeData;

    public GraphicsNode(ByteBuffer data) {
        int offset = data.position();
        index = data.getInt();
        parent = data.getInt();
        previous = data.getInt();
        next = data.getInt();
        last = data.getInt();
        data.position(offset + 0x14);
        type = data.getInt();
        if (type < 1001 && type != 1 && type != 2 && type != 3 && type != 4 && type != 8)
            Utils.getGlobalLogger().trace("GraphicsNode type {} @ {}", type, (data.position() - 4));
        data.position(offset + 0x18);
        size = data.getInt();
        data.position(offset + 0x2c);
        x = data.getShort();
        y = data.getShort();
        w = data.getShort();
        h = data.getShort();
        rotation = data.getFloat();
        scaleX = data.getFloat();
        scaleY = data.getFloat();
        xOrigin = data.getShort();
        yOrigin = data.getShort();
        data.position(offset + 0x46);
        unknown_46 = data.getShort();
        unknown_48 = data.getShort();
        unknown_4a = data.getShort();
        unknown_4c = data.getShort();
        word_4e = data.getShort();
        word_50 = data.getShort();
        data.getShort();
        unknown_54 = data.getShort();
        layer = data.getShort();
        typeData = ULD_File.getGraphicsNodeByType(type, data);
        data.position(offset + size);
    }

    @Override
    public String toString() {
        return "GraphicsNode{" +
                "index=" + index +
                ", parent=" + parent +
                ", previous=" + previous +
                ", next=" + next +
                ", last=" + last +
                ", type=" + type +
                ", size=" + size +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", rotation=" + rotation +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", xOrigin=" + xOrigin +
                ", yOrigin=" + yOrigin +
                ", unknown_46=" + unknown_46 +
                ", unknown_48=" + unknown_48 +
                ", unknown_4a=" + unknown_4a +
                ", unknown_4c=" + unknown_4c +
                ", word_4e=" + word_4e +
                ", word_50=" + word_50 +
                ", unknown_54=" + unknown_54 +
                ", layer=" + layer +
                ", typeData=" + typeData +
                "}\n";
    }
}
