package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;
import java.util.ArrayList;

//Based off of Rogueadyn's finds

class MeshPart {

    final public int indexOffset;
    final public int indexCount;
    final public int attributes;
    private final short boneReferenceOffset;
    private final short boneReferenceCount;

    public final ArrayList<Long> attributeMasks = new ArrayList<Long>();

    public MeshPart(ByteBuffer bb) {
        indexOffset = bb.getInt();
        indexCount = bb.getInt();
        attributes = bb.getInt();
        boneReferenceOffset = bb.getShort();
        boneReferenceCount = bb.getShort();
    }

}
