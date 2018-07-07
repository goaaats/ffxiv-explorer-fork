package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class GraphicsNodeTypeData_4 extends GraphicsNodeTypeData {

	public final int u5;
	public final int u2;
	public final int u4;
	public final int u3;
	public final int stretchCenter;
	public final int borderIndex;
	public final int tphdRegion;
	public final int tphdIndex;

	public GraphicsNodeTypeData_4(final ByteBuffer data) {
		super(data);
		int offset = data.position();
		tphdIndex = data.getInt();     //structEntry("tphdIndex", UNSIGNED_LONG_PACK, 0x40 + 0x18 - $offs),
		tphdRegion = data.getInt();    //structEntry("tphdRegion", UNSIGNED_LONG_PACK, 0x40 + 0x1c - $offs),
		borderIndex = data.get();      //structEntry("borderIndex", UNSIGNED_CHAR_PACK, 0x60 - $offs),
		stretchCenter = data.get();    //structEntry("stretchCenter", UNSIGNED_CHAR_PACK, 0x61 - $offs),
		//data.position(offset +
		u3 = data.getShort();    //structEntry("u3", UNSIGNED_SHORT_PACK, 0x62 - $offs),
		u4 = data.getShort();    //structEntry("u4", UNSIGNED_SHORT_PACK, 0x64 - $offs),
		u2 = data.getShort();    //structEntry("u2", UNSIGNED_SHORT_PACK, 0x66 - $offs),
		u5 = data.getShort();    //structEntry("u5", UNSIGNED_SHORT_PACK, 0x68 - $offs),
	}

	@Override
	public String toString() {
		return "GraphicsNodeTypeData_4{" +
			   "u5=" + u5 +
			   ", u2=" + u2 +
			   ", u4=" + u4 +
			   ", u3=" + u3 +
			   ", stretchCenter=" + stretchCenter +
			   ", borderIndex=" + borderIndex +
			   ", tphdRegion=" + tphdRegion +
			   ", tphdIndex=" + tphdIndex +
			   "} " + super.toString();
	}
}
