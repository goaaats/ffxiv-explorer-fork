package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

import java.io.IOException;

/*
struct tagMODEL_FILE_HEADER_BLOCK
{
    uint32_t m_uVersion;
    uint32_t m_uStackMemorySize;
    uint32_t m_uRuntimeMemorySize;
    uint16_t m_uVertexDeclarationNum;
    uint16_t m_uMaterialNum;
    uint32_t m_uVertexDataOffset[3];
    uint32_t m_uIndexDataOffset[3];
    uint32_t m_uVertexBufferSize[3];
    uint32_t m_uIndexBufferSize[3];
    uint8_t m_uLODNum;
    bool m_bEnableIndexBufferStreaming;
    bool m_bEnableEdgeGeometry;
    uint8_t m_uPadding;
}
 */

public class ModelFileSqPackData {

    //chunk decompressed sizes
    public int stackMemorySize;
    public int runtimeMemorySize;

    public final int[] vertexBufferSize = new int[3];
    public final int[] edgeGeometryVertexBufferSize = new int[3];
    public final int[] indexBufferSize = new int[3];

    //chunk sizes
    public int compressedStackMemorySize;
    public int compressedRuntimeMemorySize;

    public final int[] compressedVertexBufferSize = new int[3];
    public final int[] compressedEdgeGeometryVertexBufferSize = new int[3];
    public final int[] compressedIndexBufferSize = new int[3];

    //chunk offsets
    public int stackMemoryOffset;
    public int runtimeMemoryOffset;

    public final int[] vertexBufferOffset = new int[3];
    public final int[] edgeGeometryVertexBufferOffset = new int[3];
    public final int[] indexBufferOffset = new int[3];

    //chunk start block index
    public short stackDataBlockIndex;
    public short runtimeDataBlockIndex;

    public final short[] vertexBufferDataBlockIndex = new short[3];
    public final short[] edgeGeometryVertexBufferDataBlockIndex = new short[3];
    public final short[] indexBufferDataBlockIndex = new short[3];

    //chunk num blocks
    short stackDataBlockNum;
    short runtimeDataBlockNum;

    public final short[] vertexBufferDataBlockNum = new short[3];
    public final short[] edgeGeometryVertexBufferDataBlockNum = new short[3];
    public final short[] indexBufferDataBlockNum = new short[3];

    public short vertexDeclarationNum;
    public short materialNum;

    byte lodNum;
    byte enableIndexBufferStreaming;
    byte enableEdgeGeometry;
    byte padding;

    public ModelFileSqPackData(EARandomAccessFile file) throws IOException {
        stackMemorySize = file.readInt();
        runtimeMemorySize = file.readInt();

        for (int i = 0; i < 3; i++)
            vertexBufferSize[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            edgeGeometryVertexBufferSize[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            indexBufferSize[i] = file.readInt();

        compressedStackMemorySize = file.readInt();
        compressedRuntimeMemorySize = file.readInt();

        for (int i = 0; i < 3; i++)
            compressedVertexBufferSize[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            compressedEdgeGeometryVertexBufferSize[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            compressedIndexBufferSize[i] = file.readInt();

        stackMemoryOffset = file.readInt();
        runtimeMemoryOffset = file.readInt();

        for (int i = 0; i < 3; i++)
            vertexBufferOffset[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            edgeGeometryVertexBufferOffset[i] = file.readInt();
        for (int i = 0; i < 3; i++)
            indexBufferOffset[i] = file.readInt();

        stackDataBlockIndex = file.readShort();
        runtimeDataBlockIndex = file.readShort();

        for (int i = 0; i < 3; i++)
            vertexBufferDataBlockIndex[i] = file.readShort();
        for (int i = 0; i < 3; i++)
            edgeGeometryVertexBufferDataBlockIndex[i] = file.readShort();
        for (int i = 0; i < 3; i++)
            indexBufferDataBlockIndex[i] = file.readShort();

        stackDataBlockNum = file.readShort();
        runtimeDataBlockNum = file.readShort();

        for (int i = 0; i < 3; i++)
            vertexBufferDataBlockNum[i] = file.readShort();
        for (int i = 0; i < 3; i++)
            edgeGeometryVertexBufferDataBlockNum[i] = file.readShort();
        for (int i = 0; i < 3; i++)
            indexBufferDataBlockNum[i] = file.readShort();

        vertexDeclarationNum = file.readShort();
        materialNum = file.readShort();

        lodNum = file.readByte();
        enableIndexBufferStreaming = file.readByte();
        enableEdgeGeometry = file.readByte();
        padding = file.readByte();
    }
}
