package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteOrder;

public abstract class Game_File {

    final ByteOrder endian;

    Game_File(ByteOrder endian) {
        this.endian = endian;
    }

    public ByteOrder getEndian() {
        return endian;
    }

    public boolean isBigEndian() {
        return endian == ByteOrder.BIG_ENDIAN;
    }
}
