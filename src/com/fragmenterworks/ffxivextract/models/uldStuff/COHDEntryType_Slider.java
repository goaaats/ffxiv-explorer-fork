package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 *
 */
public class COHDEntryType_Slider extends COHDEntryType {

	public int refFill;
	public int refKnob;
	public int refValueBox;
	public int refTrack;
	public int byte_0x20;
	public int byte_0x21;
	public int byte_0x22;

	public COHDEntryType_Slider(final ByteBuffer data) {
		super(data);
		this.refFill = data.getInt();
		this.refKnob = data.getInt();
		this.refValueBox = data.getInt();
		this.refTrack = data.getInt();
		this.byte_0x20 = data.get();
		this.byte_0x21 = data.get();
		this.byte_0x22 = data.get();
	}

	@Override
	public String toString() {
		return "COHDType_Slider{" +
			   "refFill=" + refFill +
			   ", refKnob=" + refKnob +
			   ", refValueBox=" + refValueBox +
			   ", refTrack=" + refTrack +
			   ", byte_0x20=" + byte_0x20 +
			   ", byte_0x21=" + byte_0x21 +
			   ", byte_0x22=" + byte_0x22 +
			   "} " + super.toString();
	}
}
