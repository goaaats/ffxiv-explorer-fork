package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class COHDEntryType_List extends COHDEntryType {

	public final int refItemTemplate;
	public final int refScrollbar;

	public COHDEntryType_List(final ByteBuffer data) {
		super(data);
		this.refItemTemplate = data.getInt();
		this.refScrollbar = data.getInt();
	}

	@Override
	public String toString() {
		return "COHDType_List{" +
			   "itemTemplateRef=" + refItemTemplate +
			   ", scrollbarObject=" + refScrollbar +
			   "} " + super.toString();
	}
}
