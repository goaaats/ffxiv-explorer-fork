package com.fragmenterworks.ffxivextract.models.uldStuff;

import com.fragmenterworks.ffxivextract.helpers.SparseArray;

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
	private static SparseArray<Class<? extends TLHDSetTypeB_Entry_Item_Type>> tlhdBTypes = new SparseArray<>();

	public int                       type;
	public int                       w_0x6;
	public int                       size;
	public int                       count;
	public TLHDSetTypeB_Entry_Item[] frames;

	public TLHDSetTypeB_Entry(final ByteBuffer data) {
		int offset = data.position();
		type = data.getShort();
		w_0x6 = data.getShort();
		size = data.getShort();
		count = data.getShort();

		frames = new TLHDSetTypeB_Entry_Item[count];

		for ( int i = 0; i < count; i++ ) {
			frames[i] = new TLHDSetTypeB_Entry_Item(data, this);
		}

		data.position(offset + size);
	}

	private static void putTLHDAType(int kind, Class<? extends TLHDSetTypeB_Entry_Item_Type> node) {
		tlhdBTypes.put(kind, node);
	}

	public static TLHDSetTypeB_Entry_Item_Type getTLHDSetTypeAEntryItemType(int type, ByteBuffer data) {
		Class<? extends TLHDSetTypeB_Entry_Item_Type> aClass = tlhdBTypes.get(type);
		if ( aClass != null ) {
			try {
				Constructor c = aClass.getDeclaredConstructor(ByteBuffer.class);
				return (TLHDSetTypeB_Entry_Item_Type)c.newInstance(data);
			} catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e ) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static class TLHDSetTypeB_Entry_Item {
		public int                          frame;
		public int                          size;
		public TLHDSetTypeB_Entry_Item_Type typeData;

		public TLHDSetTypeB_Entry_Item(ByteBuffer data, TLHDSetTypeB_Entry typeA) {
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
