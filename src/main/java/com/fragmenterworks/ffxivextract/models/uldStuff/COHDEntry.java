package com.fragmenterworks.ffxivextract.models.uldStuff;

import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.models.ULD_File;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class COHDEntry {
	public int           index; //structEntry("index", UNSIGNED_LONG_PACK),
	public int           unknown_a; //structEntry("unkn_a", UNSIGNED_CHAR_PACK, 5),
	public int           unknown_b; //structEntry("unkn_b", UNSIGNED_CHAR_PACK, 6),
	public int           type; // structEntry("type", UNSIGNED_CHAR_PACK, 7),
	public int           count; //structEntry("count", UNSIGNED_LONG_PACK, 8),
	public int           size; //structEntry("size", UNSIGNED_SHORT_PACK, 12),
	public int           headerLength; //structEntry("headerLength", UNSIGNED_SHORT_PACK, 0xe),
	public COHDEntryType typeData;
	public final SparseArray<GraphicsNode> nodes = new SparseArray<>();

	public COHDEntry(ByteBuffer data) {
		int offset = data.position();
		index = data.getInt();
		data.get();
		unknown_a = data.get();
		unknown_b = data.get();
		type = data.get();
		count = data.getInt();
		size = data.getShort();
		headerLength = data.getShort();
		if ( headerLength > 0 ) {
			typeData = ULD_File.getCOHDNodeByType(type, data);
		}
		data.position(offset + headerLength);
		for ( int i = 0; i < count; i++ ) {
			GraphicsNode gn = new GraphicsNode(data);
			nodes.put(gn.index, gn);
		}
		data.position(offset + size);
	}

	@Override
	public String toString() {
		return "COHDEntry{" +
			   "index=" + index +
			   ", unknown_a=" + unknown_a +
			   ", unknown_b=" + unknown_b +
			   ", type=" + type +
			   ", count=" + count +
			   ", size=" + size +
			   ", headerLength=" + headerLength +
			   ", typeData=" + typeData +
			   ", nodes=" + nodes +
			   '}';
	}
}
