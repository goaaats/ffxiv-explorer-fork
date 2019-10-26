package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class GraphicsNodeTypeData_2 extends GraphicsNodeTypeData {

	public int     tphdIndex;
	public int     tphdRegion;
	public boolean flipX;
	public boolean flipY;
	public int     fillMode;
	public int     byte_0x63;

	public GraphicsNodeTypeData_2(final ByteBuffer data) {
		super(data);
		tphdIndex = data.getInt(); //structEntry("tphdIndex", UNSIGNED_LONG_PACK),
		tphdRegion = data.getInt(); //structEntry("tphdRegion", UNSIGNED_LONG_PACK),
		flipX = data.get() > 0; //structEntry("flipX", UNSIGNED_CHAR_PACK),
		flipY = data.get() > 0;// structEntry("flipY", UNSIGNED_CHAR_PACK),
		fillMode = data.get(); //structEntry("fillMode", UNSIGNED_CHAR_PACK),
		byte_0x63 = data.get(); //structEntry("b@0x63", UNSIGNED_CHAR_PACK),
	}

	@Override
	public String toString() {
		return "GraphicsNodeTypeData_2{" +
			   "tphdIndex=" + tphdIndex +
			   ", tphdRegion=" + tphdRegion +
			   ", flipX=" + flipX +
			   ", flipY=" + flipY +
			   ", fillMode=" + fillMode +
			   ", byte_0x63=" + byte_0x63 +
			   "} " + super.toString();
	}
}
