package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class COHDEntryType_Graphics extends COHDEntryType {
	int dw_0x0;
	int graphcisNode;

	public COHDEntryType_Graphics(final ByteBuffer data) {
		super(data);
		this.dw_0x0 = data.getInt();
		graphcisNode = data.getInt();
	}

	@Override
	public String toString() {
		return "COHDType_Graphics{" +
			   "dw_0x0=" + dw_0x0 +
			   ", graphcisNode=" + graphcisNode +
			   "} " + super.toString();
	}
}
