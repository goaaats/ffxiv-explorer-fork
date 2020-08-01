package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;

public class ModelFileHeader {
    public int version;
    public int stackMemorySize;
    public int runtimeMemorySize;
    public short vertexDeclarationNum;
    public short materialNum;

    public final int[] vertexDataOffset = new int[3];
    public final int[] indexDataOffset = new int[3];
    public final int[] vertexBufferSize = new int[3];
    public final int[] indexBufferSize = new int[3];

    public byte lodNum;
    public byte enableIndexBufferStreaming;
    public byte enableEdgeGeometry;
    public byte padding;

    public ModelFileHeader() {}

    public void writeOut(ByteBuffer bb) {
        bb.putInt(version);
        bb.putInt(stackMemorySize);
        bb.putInt(runtimeMemorySize);
        bb.putShort(vertexDeclarationNum);
        bb.putShort(materialNum);

        for (int i = 0; i < 3; i++)
            bb.putInt(vertexDataOffset[i]);

        for(int i = 0; i < 3; i++)
            bb.putInt(indexDataOffset[i]);

        for (int i = 0; i < 3; i++)
            bb.putInt(vertexBufferSize[i]);

        for (int i = 0; i < 3; i++)
            bb.putInt(indexBufferSize[i]);

        bb.put(lodNum);
        bb.put(enableIndexBufferStreaming);
        bb.put(enableEdgeGeometry);
        bb.put(padding);
    }

    public static ModelFileHeader read(ByteBuffer bb) {
        ModelFileHeader ret = new ModelFileHeader();
        ret.version = bb.getInt();
        ret.stackMemorySize = bb.getInt();
        ret.runtimeMemorySize = bb.getInt();
        ret.vertexDeclarationNum = bb.getShort();
        ret.materialNum = bb.getShort();

        for (int i = 0; i < 3; i++)
            ret.vertexDataOffset[i] = bb.getInt();

        for (int i = 0; i < 3; i++)
            ret.indexDataOffset[i] = bb.getInt();

        for (int i = 0; i < 3; i++)
            ret.vertexBufferSize[i] = bb.getInt();

        for (int i = 0; i < 3; i++)
            ret.indexBufferSize[i] = bb.getInt();

        ret.lodNum = bb.get();
        ret.enableIndexBufferStreaming = bb.get();
        ret.enableEdgeGeometry = bb.get();
        ret.padding = bb.get();

        return ret;
    }
}
