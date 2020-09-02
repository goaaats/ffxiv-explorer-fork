package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class ImageSetRegion {
    public final int imageIndex;
    public final int x;
    public final int y;
    public final int w;
    public final int h;

    public ImageSetRegion(final int imageIndex, final int x, final int y, final int w, final int h) {
        this.imageIndex = imageIndex;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public ImageSetRegion(ByteBuffer data) {
        this.imageIndex = data.getInt();
        this.x = (int) data.getShort() & 0xFFFF;
        this.y = (int) data.getShort() & 0xFFFF;
        this.w = (int) data.getShort() & 0xFFFF;
        this.h = (int) data.getShort() & 0xFFFF;
    }

    @Override
    public String toString() {
        return "ImageSetRegion{" +
                "imageIndex=" + imageIndex +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                "}\n";
    }
}
