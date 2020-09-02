package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataBlock {

    private long offset = 0;
    private int size;
    private int version;
    private int compressedSize;
    private int decompressedSize;

    private byte[] compressedData;
    private byte[] decompressedData;
    private boolean mustBeDecompressed;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public int getDecompressedSize() {
        return decompressedSize;
    }

    public void setDecompressedSize(int decompressedSize) {
        this.decompressedSize = decompressedSize;
    }

    public DataBlock(EARandomAccessFile file, int type) throws IOException {
        offset = file.getFilePointer();
        size = file.readInt();
        version = file.readInt();

        if (type == 2) {
            int tmp = file.readInt();
            compressedSize = tmp & 0xFFFF;
            decompressedSize = tmp >> 16 & 0xFF;
        } else {
            compressedSize = file.readInt();
            decompressedSize = file.readInt();
        }

        mustBeDecompressed = !(compressedSize == 32000 || decompressedSize == 1);

        if (!mustBeDecompressed)
            compressedSize = decompressedSize;

        compressedData = new byte[compressedSize];
        decompressedData = new byte[decompressedSize];

        file.readFully(compressedData, 0, compressedSize);
    }

    /**
     * Decompresses this data block if necessary.
     * @return the data from this data block
     */
    public byte[] getData() {

        if (!mustBeDecompressed)
            return compressedData;

        // Build the zlib header stuff
        byte[] gzData = new byte[compressedSize + 6]; // Need to add 6 bytes

        // for missing zlib
        // header/footer
        // Zlib Magic Number
        gzData[0] = (byte) 0x78;
        gzData[1] = (byte) 0x9C;

        // Actual Data
        System.arraycopy(compressedData, 0, gzData, 2, compressedSize);

        // Checksum
        int checksum = adler32(gzData, 2, compressedSize);
        byte[] checksumByte = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(checksum).array();
        System.arraycopy(checksumByte, 0, gzData, 2 + compressedSize, 4);

        //Decompress this block
        try {
            Inflater inflater = new Inflater();

            inflater.setInput(gzData);
            inflater.setOutput(decompressedData);

            int err = inflater.init();
            CHECK_ERR(inflater, err, "inflateInit");

            while (inflater.total_out < decompressedSize && inflater.total_in < gzData.length) {
                inflater.avail_in = inflater.avail_out = 1;
                //Force small buffers?
                err = inflater.inflate(JZlib.Z_NO_FLUSH);
                if (err == JZlib.Z_STREAM_END)
                    break;
                CHECK_ERR(inflater, err, "inflate");
            }

            err = inflater.end();
            CHECK_ERR(inflater, err, "inflateEnd");

            inflater.finished();

            return decompressedData;
        } catch (Exception e) {
            Utils.getGlobalLogger().error(e);
        }
        return null;
    }

    private static void CHECK_ERR(Inflater z, int err, String msg) {
        if (err != JZlib.Z_OK) {
            if (z.msg != null)
                Utils.getGlobalLogger().error("Inflater error: {}", z.msg);
            Utils.getGlobalLogger().error("{} error: {}", msg, err);
            System.exit(1);
        }
    }

    private int adler32(byte[] bytes, int offset, int size) {
        final int a32mod = 65521;
        int s1 = 1, s2 = 0;

        for (int i = offset; i < size; i++) {
            int b = bytes[i];

            s1 = (s1 + b) % a32mod;
            s2 = (s2 + s1) % a32mod;
        }
        return (s2 << 16) + s1;
    }
}
