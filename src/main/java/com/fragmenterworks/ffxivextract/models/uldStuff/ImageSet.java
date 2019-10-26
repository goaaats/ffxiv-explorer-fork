package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class ImageSet {
	public int              index;
	public int              regionCount;
	public int              unknown;
	public ImageSetRegion[] regions;

	public ImageSet(final int index) {
		this.index = index;
	}

	public ImageSet(final ByteBuffer data) {
		index = data.getInt() & 0xFFFF;
		regionCount = data.getInt() & 0xFFFF;
		unknown = data.getInt() & 0xFFFF;
		regions = new ImageSetRegion[regionCount];
		for ( int i = 0; i < regionCount; i++ ) {
			regions[i] = new ImageSetRegion(data);
		}
	}

	@Override
	public String toString() {
		return "ImageSet{" +
			   "index=" + index +
			   ", regionCount=" + regionCount +
			   ", unknown=" + unknown +
			   ", regions=" + Arrays.toString(regions) +
			   "}\n";
	}
}
