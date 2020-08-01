package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.jogamp.common.nio.Buffers;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Mesh {

    private final int numBuffers;
    public final ByteBuffer[] vertBuffers;
    public final ByteBuffer indexBuffer;
    private final int[] vertexBufferOffsets;
    final public int[] vertexSizes;
    final public int indexBufferOffset;
    final public int numVerts, numIndex;
    final public int partTableOffset, partTableCount;
    final public int boneListIndex;
    final public int materialNumber;
    private final int vertElementIndex;

    public Mesh(ByteBuffer bb, int elementIndex) {
        numVerts = bb.getShort();
        bb.getShort();
        numIndex = bb.getInt();

        materialNumber = bb.getShort();
        partTableOffset = bb.getShort();
        partTableCount = bb.getShort();
        boneListIndex = bb.getShort();

        indexBufferOffset = bb.getInt();

        //Seems FFXIV already stores the offset of the aux buffer (and others). DOH! Learned from Saint Coinach...
        vertexBufferOffsets = new int[3];
        for (int x = 0; x < vertexBufferOffsets.length; x++)
            vertexBufferOffsets[x] = bb.getInt();

        vertexSizes = new int[3];
        for (int x = 0; x < vertexSizes.length; x++)
            vertexSizes[x] = bb.get() & 0xFF;

        numBuffers = bb.get() & 0xFF;

        vertElementIndex = elementIndex;

        vertBuffers = new ByteBuffer[numBuffers];

        for (int i = 0; i < numBuffers; i++)
            vertBuffers[i] = Buffers.newDirectByteBuffer(numVerts * vertexSizes[i]);
        indexBuffer = Buffers.newDirectByteBuffer(numIndex * 2);

        Utils.getGlobalLogger().trace("Num parts: {}\n\tNum verts: {}\n\tNum indices: {}\n\tVertex offset: {}\n\tIndex offset: {}",
                partTableCount, numVerts, numIndex, vertexBufferOffsets[0], indexBufferOffset);
    }

    public void loadMeshes(ByteBuffer bb, int lodVertexOffset, int lodIndexOffset) throws BufferOverflowException, BufferUnderflowException {

        ByteBuffer bbTemp;
        boolean swapBuffers = false;

        //Vert Table
        for (int i = 0; i < numBuffers; i++) {
            bb.position(lodVertexOffset + vertexBufferOffsets[i]);
            bbTemp = bb.duplicate();
            bbTemp.limit(bbTemp.position() + ((vertexSizes[i] * numVerts)));

            if (bb.order() == ByteOrder.BIG_ENDIAN && swapBuffers) {
                byte[] newTmp = reverseElements(bbTemp.array(), bbTemp.position(), bbTemp.limit() - bbTemp.position(), 2, (bbTemp.limit() - bbTemp.position()) / 2);
                vertBuffers[i].put(newTmp);
            } else {
                vertBuffers[i].put(bbTemp);
            }
        }
        //Index Table
        bb.position(lodIndexOffset + (indexBufferOffset * 2));
        bbTemp = bb.duplicate();
        bbTemp.limit(bbTemp.position() + (2 * numIndex));

        if (bb.order() == ByteOrder.BIG_ENDIAN && swapBuffers) {
            byte[] newTmp = reverseElements(bbTemp.array(), bbTemp.position(), bbTemp.limit() - bbTemp.position(), 2, numIndex);
            indexBuffer.put(newTmp);
        } else {
            indexBuffer.put(bbTemp);
        }
    }

    public int getVertexElementIndex() {
        return vertElementIndex;
    }

    private byte[] reverseElements(byte[] src, int start, int length, int elemSize, int elemAmt) {
        byte[] dest = new byte[length];
        byte[] tmp = new byte[length];
        System.arraycopy(src, start, tmp, 0, length);

        for (int i = 0; i < elemAmt; i++) {
            for (int j = elemSize - 1; j >= 0; j--) {
                int destOffset = i * elemSize + (elemSize - j - 1);
                int srcOffset = start + (i * elemSize + j);
                int tmpInternalSrcOffset = srcOffset - start;
                dest[destOffset] = src[srcOffset];
            }
        }

        return dest;
    }
}
