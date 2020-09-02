package com.fragmenterworks.ffxivextract.models.uldStuff;

import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.helpers.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class TLHDSetTypeB_Entry {
    private static final SparseArray<Class<? extends TLHDSetTypeB_Entry_Item_Type>> tlhdBTypes = new SparseArray<>();

    private final int type;
    private final int w_0x6;
    private final int size;
    private final int count;
    private final TLHDSetTypeB_Entry_Item[] frames;

    public TLHDSetTypeB_Entry(final ByteBuffer data) {
        int offset = data.position();
        type = data.getShort();
        w_0x6 = data.getShort();
        size = data.getShort();
        count = data.getShort();

        frames = new TLHDSetTypeB_Entry_Item[count];

        for (int i = 0; i < count; i++) {
            frames[i] = new TLHDSetTypeB_Entry_Item(data, this);
        }

        data.position(offset + size);
    }

    private static void putTLHDAType(int kind, Class<? extends TLHDSetTypeB_Entry_Item_Type> node) {
        tlhdBTypes.put(kind, node);
    }

    private static TLHDSetTypeB_Entry_Item_Type getTLHDSetTypeAEntryItemType(int type, ByteBuffer data) {
        Class<? extends TLHDSetTypeB_Entry_Item_Type> aClass = tlhdBTypes.get(type);
        if (aClass != null) {
            try {
                Constructor c = aClass.getDeclaredConstructor(ByteBuffer.class);
                return (TLHDSetTypeB_Entry_Item_Type) c.newInstance(data);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                Utils.getGlobalLogger().error(e);
            }
        }
        return null;
    }

    public static class TLHDSetTypeB_Entry_Item {
        final int frame;
        final int size;
        final TLHDSetTypeB_Entry_Item_Type typeData;

        TLHDSetTypeB_Entry_Item(ByteBuffer data, TLHDSetTypeB_Entry typeA) {
            int offset = data.position();
            frame = data.getInt();
            size = data.getShort();
            typeData = getTLHDSetTypeAEntryItemType(typeA.type, data);
            data.position(offset + size);
        }

        @Override
        public String toString() {
            return "TLHDSetTypeB_Entry_Item{" +
                    "frame=" + frame +
                    ", size=" + size +
                    ", typeData=" + typeData +
                    "}\n";
        }
    }

    public static abstract class TLHDSetTypeB_Entry_Item_Type {
        public TLHDSetTypeB_Entry_Item_Type(ByteBuffer data) {

        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Override
    public String toString() {
        return "TLHDSetTypeB_Entry{" +
                "type=" + type +
                ", w_0x6=" + w_0x6 +
                ", size=" + size +
                ", count=" + count +
                ", frames=" + Arrays.toString(frames) +
                "}\n";
    }
}
