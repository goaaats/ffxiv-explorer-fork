package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class GraphicsNodeTypeData_3 extends GraphicsNodeTypeData {

	public final int dword_0x58;
	public final int foreground_r;
	public final int foreground_g;
	public final int foreground_b;
	public final int foreground_a;
	public final int   fontSize;
	//public final int   u5;
	//public final int   u4;
	public final int   u3;
	//public final int   u2;
	public final int background_r;
	public final int background_g;
	public final int background_b;
	public final int background_a;
	public final int fontNumber;
	public final int stringIndex;

	public GraphicsNodeTypeData_3(final ByteBuffer data) {
		super(data);
		int offset = data.position();
		dword_0x58 = data.getInt(); //structEntry("dw@58", UNSIGNED_LONG_PACK, 0x58 - $offs),
		foreground_r = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		foreground_g = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		foreground_b = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		foreground_a = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		stringIndex = data.get(); //structEntry("u2", UNSIGNED_CHAR_PACK, 0x60 - $offs),
		u3 = data.get(); //structEntry("u3", UNSIGNED_CHAR_PACK, 0x61 - $offs),
		fontNumber = (int)data.get() & 0xff; //structEntry("u4", UNSIGNED_CHAR_PACK, 0x62 - $offs),
		fontSize = (int)data.get(); //structEntry("u5", UNSIGNED_CHAR_PACK, 0x63 - $offs),
		background_r = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		background_g = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		background_b = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		background_a = (int)data.get() & 0xff; //structEntry("color1", STRUCT_PACK, 0x5c - $offs, 0, 1, COLOR_STRUCT(0x5c), null, null, true),
		//fontNumber = data.getShort(); //structEntry("color2", STRUCT_PACK, 0x64 - $offs, 0, 1, COLOR_STRUCT(0x64), null, null, true),
		data.position(offset + 19);
		//fontSize = data.get(); //structEntry("u7", UNSIGNED_CHAR_PACK, 0x6b - $offs),
	}

	@Override
	public String toString() {
		return "GraphicsNodeTypeData_3{" +
			   "dword_0x58=" + dword_0x58 +
			   ", foreground=" + foreground_r + "-" + foreground_a + "-" + foreground_b + "-" + foreground_a +
			   ", fontSize=" + fontSize +
			   ", background=" + background_r + "-" + background_g + "-" + background_b + "-" + background_a +
			   ", u3=" + u3 +
			   ", fontNumber=" + fontNumber +
			   "} " + super.toString();
	}
}
