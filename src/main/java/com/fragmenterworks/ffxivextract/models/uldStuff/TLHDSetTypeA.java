package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class TLHDSetTypeA {

    private final int startFrame;
    private final int endFrame;
    private final int size;
    private final int count;

    private final TLHDSetTypeA_Entry[] entries;

    public TLHDSetTypeA(ByteBuffer data) {
        int offset = data.position();
        this.startFrame = data.getInt();
        this.endFrame = data.getInt();
        this.size = data.getInt();
        this.count = data.getInt();

        entries = new TLHDSetTypeA_Entry[count];

        for (int i = 0; i < count; i++) {
            entries[i] = new TLHDSetTypeA_Entry(data);
        }
        data.position(offset + size);
    }

    @Override
    public String toString() {
        return "TLHDSetTypeA{" +
                "startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", size=" + size +
                ", count=" + count +
                ", entries=" + Arrays.toString(entries) +
                "}\n";
    }
}
