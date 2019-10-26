package com.fragmenterworks.ffxivextract.models.uldStuff;

import com.fragmenterworks.ffxivextract.helpers.SparseArray;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class WDHDEntry {
	public int index; //structEntry("index", UNSIGNED_LONG_PACK),
	public int unknown_0x4; //structEntry("index", UNSIGNED_LONG_PACK),
	public int width; //structEntry("unkn_a", UNSIGNED_CHAR_PACK, 5),
	public int height; //structEntry("unkn_b", UNSIGNED_CHAR_PACK, 6),
	public int type; // structEntry("type", UNSIGNED_CHAR_PACK, 7),
	public int count; //structEntry("count", UNSIGNED_LONG_PACK, 8),
	public int size; //structEntry("size", UNSIGNED_SHORT_PACK, 12),

	public final SparseArray<GraphicsNode> nodes = new SparseArray<>();

	public WDHDEntry(ByteBuffer data) {
		int offset = data.position();
		index = data.getInt();
		unknown_0x4 = data.getInt();
		width = data.getShort();
		height = data.getShort();
		count = data.getShort();
		size = data.getShort();

		for ( int i = 0; i < count; i++ ) {
			GraphicsNode gn = new GraphicsNode(data);
			nodes.put(gn.index, gn);
		}
		data.position(offset + size);
	}

	@Override
	public String toString() {
		return "WDHDEntry{" +
			   "index=" + index +
			   ", unknown_0x4=" + unknown_0x4 +
			   ", width=" + width +
			   ", height=" + height +
			   ", type=" + type +
			   ", count=" + count +
			   ", size=" + size +
			   ", nodes=" + nodes +
			   '}';
	}
}
