package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class TLHDSet {
	public int index;
	public int size;
	public int a_count;
	public int b_count;

	public TLHDSetTypeA[] As;
	public TLHDSetTypeB[] Bs;

	public TLHDSet(ByteBuffer data) {
		int offset = data.position();
		index = data.getInt();
		size = data.getInt();
		a_count = data.getShort();
		b_count = data.getShort();

		As = new TLHDSetTypeA[a_count];
		Bs = new TLHDSetTypeB[b_count];

		for(int i = 0; i < a_count; i++){
			As[i] = new TLHDSetTypeA(data);
		}
		for(int i = 0; i < b_count; i++){
			Bs[i] = new TLHDSetTypeB(data);
		}
		data.position(offset + size);
	}

	@Override
	public String toString() {
		return "TLHDSet{" +
			   "index=" + index +
			   ", size=" + size +
			   ", a_count=" + a_count +
			   ", b_count=" + b_count +
			   ", As=" + Arrays.toString(As) +
			   ", Bs=" + Arrays.toString(Bs) +
			   "}\n";
	}
}
