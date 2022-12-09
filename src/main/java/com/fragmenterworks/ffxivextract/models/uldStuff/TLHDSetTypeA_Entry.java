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
public class TLHDSetTypeA_Entry {
    private static final SparseArray<Class<? extends TLHDSetTypeA_Entry_Item_Type>> tlhdATypes = new SparseArray<>();

    static {
        putTLHDAType(1, TLHDSetTypeA_Entry_Item_Type_1.class);
        putTLHDAType(2, TLHDSetTypeA_Entry_Item_Type_2.class);
        putTLHDAType(3, TLHDSetTypeA_Entry_Item_Type_3.class);
        putTLHDAType(4, TLHDSetTypeA_Entry_Item_Type_4.class);
        putTLHDAType(5, TLHDSetTypeA_Entry_Item_Type_5.class);
    }

    private int type;
    private int w_0x6;
    private int size;
    private int count;
    private TLHDSetTypeA_Entry_Item[] frames;

    public TLHDSetTypeA_Entry(final ByteBuffer data) {
        int offset = data.position();
        type = data.getShort();
        w_0x6 = data.getShort();
        size = data.getShort();
        count = data.getShort();

        frames = new TLHDSetTypeA_Entry_Item[count];

        for (int i = 0; i < count; i++) {
            frames[i] = new TLHDSetTypeA_Entry_Item(data, this);
        }

        data.position(offset + size);
    }

    private static void putTLHDAType(int kind, Class<? extends TLHDSetTypeA_Entry_Item_Type> node) {
        tlhdATypes.put(kind, node);
    }

    private static TLHDSetTypeA_Entry_Item_Type getTLHDSetTypeAEntryItemType(int type, ByteBuffer data) {
        Class<? extends TLHDSetTypeA_Entry_Item_Type> aClass = tlhdATypes.get(type);
        if (aClass != null) {
            try {
                Constructor c = aClass.getDeclaredConstructor(ByteBuffer.class);
                return (TLHDSetTypeA_Entry_Item_Type) c.newInstance(data);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                Utils.getGlobalLogger().error("", e);
            }
        }
        return null;
    }

    public static class TLHDSetTypeA_Entry_Item {
        final int frame;
        final int size;
        final TLHDSetTypeA_Entry_Item_Type typeData;

        TLHDSetTypeA_Entry_Item(ByteBuffer data, TLHDSetTypeA_Entry typeA) {
            int offset = data.position();
            frame = data.getInt();
            size = data.getShort();
            typeData = getTLHDSetTypeAEntryItemType(typeA.type, data);
            data.position(offset + size);
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item{" +
                    "frame=" + frame +
                    ", size=" + size +
                    ", typeData=" + typeData +
                    "}\n";
        }
    }

    public static abstract class TLHDSetTypeA_Entry_Item_Type {
        TLHDSetTypeA_Entry_Item_Type(ByteBuffer data) {

        }

        @Override
        public String toString() {
            return "";
        }
    }

    public static class TLHDSetTypeA_Entry_Item_Type_1 extends TLHDSetTypeA_Entry_Item_Type {

        final int unk_node;
        final float float_0x06;
        final float float_0x0a;

        TLHDSetTypeA_Entry_Item_Type_1(final ByteBuffer data) {
            super(data);
            int offset = data.position();
            unk_node = data.getShort();
            data.position(offset + 0x6);
            float_0x06 = data.getFloat();
            data.position(offset + 0xa);
            float_0x0a = data.getFloat();
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item_Type_1{" +
                    "unk_node=" + unk_node +
                    ", float_0x06=" + float_0x06 +
                    ", float_0x0a=" + float_0x0a +
                    "}\n" + super.toString();
        }
    }

    public static class TLHDSetTypeA_Entry_Item_Type_2 extends TLHDSetTypeA_Entry_Item_Type {

        final int unk_node;
        final float float_0x02;
        final float float_0x06;
        final float float_0x0a;
        final float float_0x0e;

        TLHDSetTypeA_Entry_Item_Type_2(final ByteBuffer data) {
            super(data);
            int offset = data.position();
            unk_node = data.getShort();
            data.position(offset + 0x2);
            float_0x02 = data.getFloat();
            float_0x06 = data.getFloat();
            float_0x0a = data.getFloat();
            float_0x0e = data.getFloat();
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item_Type_2{" +
                    "unk_node=" + unk_node +
                    ", float_0x02=" + float_0x02 +
                    ", float_0x06=" + float_0x06 +
                    ", float_0x0a=" + float_0x0a +
                    ", float_0x0e=" + float_0x0e +
                    "}\n" + super.toString();
        }
    }

    public static class TLHDSetTypeA_Entry_Item_Type_3 extends TLHDSetTypeA_Entry_Item_Type {

        final int unk_node;
        final float float_0x06;
        final float word_0x0a;
        final float word_0x0c;

        TLHDSetTypeA_Entry_Item_Type_3(final ByteBuffer data) {
            super(data);
            int offset = data.position();
            unk_node = data.getShort();
            data.position(offset + 0x6);
            float_0x06 = data.getFloat();
            data.position(offset + 0xa);
            word_0x0a = data.getShort();
            word_0x0c = data.getShort();
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item_Type_3{" +
                    "unk_node=" + unk_node +
                    ", float_0x06=" + float_0x06 +
                    ", word_0x0a=" + word_0x0a +
                    ", word_0x0c=" + word_0x0c +
                    "}\n" + super.toString();
        }
    }

    /**
     *
     */
    public static class TLHDSetTypeA_Entry_Item_Type_4 extends TLHDSetTypeA_Entry_Item_Type {

        final int unk_node;
        final float float_0x02;
        final float float_0x06;
        final float word_0x0a;
        final float word_0x0c;
        final float word_0x0e;
        final float word_0x10;
        final float word_0x12;
        final float word_0x14;

        TLHDSetTypeA_Entry_Item_Type_4(final ByteBuffer data) {
            super(data);
            int offset = data.position();
            unk_node = data.getShort();
            data.position(offset + 0x2);
            float_0x02 = data.getFloat();
            float_0x06 = data.getFloat();
            word_0x0a = data.getShort();
            word_0x0c = data.getShort();
            word_0x0e = data.getShort();
            word_0x10 = data.getShort();
            word_0x12 = data.getShort();
            word_0x14 = data.getShort();
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item_Type_4{" +
                    "unk_node=" + unk_node +
                    ", float_0x02=" + float_0x02 +
                    ", float_0x06=" + float_0x06 +
                    ", word_0x0a=" + word_0x0a +
                    ", word_0x0c=" + word_0x0c +
                    ", word_0x0e=" + word_0x0e +
                    ", word_0x10=" + word_0x10 +
                    ", word_0x12=" + word_0x12 +
                    ", word_0x14=" + word_0x14 +
                    "}\n" + super.toString();
        }
    }

    /**
     *
     */
    public static class TLHDSetTypeA_Entry_Item_Type_5 extends TLHDSetTypeA_Entry_Item_Type {

        final int unk_node;
        final float float_0x06;
        final int color1_r;
        final int color1_g;
        final int color1_b;
        final int color1_a;

        TLHDSetTypeA_Entry_Item_Type_5(final ByteBuffer data) {
            super(data);
            int offset = data.position();
            unk_node = data.getShort();
            data.position(offset + 0x2);
            float_0x06 = data.getFloat();
            color1_r = data.getShort();
            color1_g = data.getShort();
            color1_b = data.getShort();
            color1_a = data.getShort();
        }

        @Override
        public String toString() {
            return "TLHDSetTypeA_Entry_Item_Type_5{" +
                    "unk_node=" + unk_node +
                    ", float_0x06=" + float_0x06 +
                    ", color1_r=" + color1_r +
                    ", color1_g=" + color1_g +
                    ", color1_b=" + color1_b +
                    ", color1_a=" + color1_a +
                    "}\n" + super.toString();
        }
    }

    @Override
    public String toString() {
        return "TLHDSetTypeA_Entry{" +
                "type=" + type +
                ", w_0x6=" + w_0x6 +
                ", size=" + size +
                ", count=" + count +
                ", frames=" + Arrays.toString(frames) +
                "}\n";
    }
}
