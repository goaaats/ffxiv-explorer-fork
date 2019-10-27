package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.gui.components.Loading_Dialog;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

public class SqPack_DatFile {

    private final static int TYPE_TEXTURE = 4;
    private final static int TYPE_MODEL = 3;
    private final static int TYPE_BINARY = 2;
    public final static int TYPE_PLACEHOLDER = 1;

    private EARandomAccessFile currentFilePointer;
    private final ByteOrder endian;

    SqPack_DatFile(String path, ByteOrder endian) throws FileNotFoundException {
        this.endian = endian;
        currentFilePointer = new EARandomAccessFile(path, "r", endian);
    }

    @SuppressWarnings("unused")
    protected byte[] extractFile(long fileOffset, Loading_Dialog loadingDialog) throws IOException {
        currentFilePointer.seek(fileOffset);
        int headerLength = currentFilePointer.readInt();
        int contentType = currentFilePointer.readInt();
        int fileSize = currentFilePointer.readInt();
        currentFilePointer.readInt(); // UNKNOWN
        int blockBufferSize = currentFilePointer.readInt() * 0x80;
        int blockCount = currentFilePointer.readInt();

        byte[] extraHeader = null;
        int extraHeaderSize = 0;

        Utils.getGlobalLogger().trace("\nFile @ {}\n\tHeader length: {}\n\tContent type: {}\n\tFile size: {}\n\tBlock buffer size: {}",
                String.format("File @ %08x", fileOffset), headerLength, contentType, fileSize, blockBufferSize);

        Utils.getGlobalLogger().trace("Block data: ...");

        Data_Block[][] dataBlocks = null;

        //How to get the blocks
        switch (contentType) {
            case TYPE_TEXTURE:

                TextureBlocks[] blocks = new TextureBlocks[blockCount];

                dataBlocks = new Data_Block[blockCount][];

                for (int i = 0; i < blockCount; i++) {
                    int frameStartOffset = currentFilePointer.readInt();
                    int frameSize = currentFilePointer.readInt();
                    int wut1 = currentFilePointer.readInt();
                    int blockTableOffset = currentFilePointer.readInt();
                    int numSubBlocks = currentFilePointer.readInt();
                    blocks[i] = new TextureBlocks(frameStartOffset, frameSize, blockTableOffset, numSubBlocks);
                    dataBlocks[i] = new Data_Block[numSubBlocks];
                }

                for (int i = 0; i < blockCount; i++) {
                    TextureBlocks block = blocks[i];
                    dataBlocks[i][0] = new Data_Block(block.offset);
                    int runningTotal = block.offset;
                    for (int j = 1; j < block.subblockSize; j++) {
                        runningTotal += currentFilePointer.readShort();
                        dataBlocks[i][j] = new Data_Block(runningTotal);
                    }
                    currentFilePointer.readShort();
                }

                extraHeaderSize = blocks[0].offset;
                extraHeader = new byte[extraHeaderSize];
                currentFilePointer.seek(fileOffset + headerLength);
                currentFilePointer.read(extraHeader, 0, extraHeaderSize);

                break;
            case TYPE_MODEL:

                ContentType3Container container = new ContentType3Container();

                for (int i = 0; i < 11; i++)
                    container.chunkDecompressedSizes[i] = currentFilePointer.readInt();
                for (int i = 0; i < 11; i++)
                    container.chunkSizes[i] = currentFilePointer.readInt();
                for (int i = 0; i < 11; i++)
                    container.chunkOffsets[i] = currentFilePointer.readInt();
                for (int i = 0; i < 11; i++)
                    container.chunkStartBlockIndex[i] = currentFilePointer.readShort();
                int numBlocks = 0;
                for (int i = 0; i < 11; i++) {
                    container.chunkNumBlocks[i] = currentFilePointer.readShort();
                    numBlocks += container.chunkNumBlocks[i];
                }

                container.blockSizes = new short[numBlocks];

                //Skip, unknown
                container.numMeshes = currentFilePointer.readShort();
                container.numMaterials = currentFilePointer.readShort();
                short y1 = currentFilePointer.readShort();
                short y2 = currentFilePointer.readShort();

                for (int i = 0; i < numBlocks; i++)
                    container.blockSizes[i] = currentFilePointer.readShort();

                //int CHOSEN_CHUNK = 2;

                int pos = 0x44;
                byte[] mdlData = new byte[fileSize];
                ByteBuffer bb = ByteBuffer.wrap(mdlData);

                bb.order(endian);
                bb.putShort(container.numMeshes);
                bb.putShort(container.numMaterials);
                currentFilePointer.seek(fileOffset + headerLength + container.chunkOffsets[0]);
                for (int i = 0; i < container.blockSizes.length; i++) {
                    int lastPos = (int) currentFilePointer.getFilePointer();

                    // Block Header
                    int blockHeaderLength2 = currentFilePointer.readInt();
                    currentFilePointer.readInt(); // NULL
                    int compressedBlockSize2 = currentFilePointer.readInt();
                    int decompressedBlockSize2 = currentFilePointer.readInt();

                    byte[] decompressedBlock2 = null;
                    if (compressedBlockSize2 == 32000 || decompressedBlockSize2 == 1) //Not actually compressed, just read decompressed size
                    {
                        decompressedBlock2 = new byte[decompressedBlockSize2];
                        currentFilePointer.readFully(decompressedBlock2);
                    } else //Gotta decompress
                        decompressedBlock2 = decompressBlock(compressedBlockSize2, decompressedBlockSize2);

                    System.arraycopy(decompressedBlock2, 0, mdlData, pos, decompressedBlockSize2);
                    pos += decompressedBlockSize2;

                    currentFilePointer.seek(lastPos + container.blockSizes[i]);
                }
                return mdlData;
            case TYPE_BINARY:
                dataBlocks = new Data_Block[1][blockCount];

                // Read in Block Info Header
                for (int i = 0; i < blockCount; i++) {
                    int offset = currentFilePointer.readInt();
                    int paddingAndSize = currentFilePointer.readInt();
                    int padding = paddingAndSize & 0xFFFF;
                    int decompressedBlockSize = paddingAndSize >> 16 & 0xFFFF;

                    dataBlocks[0][i] = new Data_Block(offset, padding, decompressedBlockSize);

                    Utils.getGlobalLogger().trace("Block #{}\n\tOffset: {}\n\tPadding: {}\n\tUncompressed size: {}",
                            i, String.format("%X", offset), padding, decompressedBlockSize);
                }
                break;
        }

        byte[] decompressedFile = null;
        int currentFileOffset = -1;

        try {
            if (fileSize + extraHeaderSize < 0)
                return null;

            decompressedFile = new byte[fileSize];
        } catch (Exception e) {
            return null;
        }

        //If we got a loading dialog
        if (loadingDialog != null)
            loadingDialog.setMaxBlocks(dataBlocks[0].length);

        if (dataBlocks == null || dataBlocks[0] == null)
            return null;

        int[] mipmapPosTable = null;

        //Load in file offsets for mipmaps
        if (extraHeader != null && contentType == TYPE_TEXTURE) {
            ByteBuffer bb = ByteBuffer.wrap(extraHeader);
            bb.order(endian);

            bb.position(0x0E);
            int numMipmaps = bb.getShort();
            mipmapPosTable = new int[numMipmaps];

            if (numMipmaps < blockCount)
                mipmapPosTable = new int[blockCount];

            bb.position(0x1C);
            for (int i = 0; i < numMipmaps; i++)
                mipmapPosTable[i] = bb.getInt();
        }

        //Extract File
        for (int j = 0; j < (contentType == TYPE_TEXTURE ? blockCount : 1); j++) {
            if (mipmapPosTable != null)
                currentFileOffset = mipmapPosTable[j];
            else
                currentFileOffset = 0;

            for (int i = 0; i < dataBlocks[j].length; i++) {
                // Block Header
                currentFilePointer.seek(fileOffset + headerLength + dataBlocks[j][i].offset);
                int blockHeaderLength = currentFilePointer.readInt();
                currentFilePointer.readInt(); // NULL
                int compressedBlockSize = currentFilePointer.readInt();
                int decompressedBlockSize = currentFilePointer.readInt();

                Utils.getGlobalLogger().trace("Decompressing block {} @ file offset {} @ block offset: {}",
                        i, currentFilePointer.getFilePointer(), String.format("%X", dataBlocks[j][i].offset));
                Utils.getGlobalLogger().trace("Compressed size: {}, decompressed size: {}, block size: {}",
                        compressedBlockSize, decompressedBlockSize, dataBlocks[j][i].padding);

                byte[] decompressedBlock = null;
                if (compressedBlockSize == 32000 || decompressedBlockSize == 1) //Not actually compressed, just read decompressed size
                {
                    decompressedBlock = new byte[decompressedBlockSize];
                    currentFilePointer.readFully(decompressedBlock);
                } else //Gotta decompress
                    decompressedBlock = decompressBlock(compressedBlockSize, decompressedBlockSize);

                try {
                    System.arraycopy(decompressedBlock, 0, decompressedFile, currentFileOffset, decompressedBlockSize);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Couldn't tell you
//					Utils.getGlobalLogger().error(e);
                }

                currentFileOffset += decompressedBlockSize;

                if (loadingDialog != null)
                    loadingDialog.nextBlock(i + 1);
            }
        }
        if (extraHeader != null)
            System.arraycopy(extraHeader, 0, decompressedFile, 0, extraHeaderSize);

        return decompressedFile;
    }

    @SuppressWarnings("deprecation")
    private byte[] decompressBlock(int compressedSize, int decompressedSize) throws IOException {

        // Build the zlib header stuff
        byte[] decompressedData = new byte[decompressedSize];
        byte[] gzipedData = new byte[compressedSize + 6]; // Need to add 6 bytes
        // for missing zlib
        // header/footer
        // Zlib Magic Number
        gzipedData[0] = (byte) 0x78;
        gzipedData[1] = (byte) 0x9C;

        // Actual Data
        currentFilePointer.readFully(gzipedData, 2, compressedSize);

        // Checksum
        int checksum = adler32(gzipedData, 2, compressedSize);
        byte[] checksumByte = ByteBuffer.allocate(4)
                .order(ByteOrder.BIG_ENDIAN).putInt(checksum).array();
        System.arraycopy(checksumByte, 0, gzipedData, 2 + compressedSize, 4);

        //Decompress this block
        try {

            Inflater inflater = new Inflater();

            inflater.setInput(gzipedData);
            inflater.setOutput(decompressedData);

            int err = inflater.init();
            CHECK_ERR(inflater, err, "inflateInit");

            while (inflater.total_out < decompressedSize
                    && inflater.total_in < gzipedData.length) {
                inflater.avail_in = inflater.avail_out = 1; /*
                 * force small
                 * buffers
                 */
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

    void close() throws IOException {
        currentFilePointer.close();
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

    class Data_Block {
        final int offset;
        final int padding;
        final int decompressedSize;

        Data_Block(int offset, int padding, int decompressedSize) {
            this.offset = offset;
            this.padding = padding;
            this.decompressedSize = decompressedSize;
        }

        Data_Block(int offset) {
            this.offset = offset;
            this.padding = -1;
            this.decompressedSize = -1;
        }
    }

    protected class TextureBlocks {
        final int offset;
        final int padding;
        final int tableOffset;
        final int subblockSize;

        TextureBlocks(int offset, int padding, int tableOffset, int subblocksize) {
            this.offset = offset;
            this.padding = padding;
            this.tableOffset = tableOffset;
            this.subblockSize = subblocksize;
        }

    }

    int getContentType(long offset) throws IOException {
        currentFilePointer.seek(offset);
        currentFilePointer.readInt(); //Header Length
        return currentFilePointer.readInt();
    }

    Calendar getTimeStamp() throws IOException {
        // Timestamp
        currentFilePointer.seek(0x18);
        int date = currentFilePointer.readInt();
        int time = currentFilePointer.readInt();
        if (date != 0) {
            //Copied code from Cassiope example, too lazy to write my own lol.
            int yyyy = (date / 10000) % 10000;
            int mm = (date / 100) % 100;
            int dd = date % 100;
            int hh24 = (time / 1000000) % 100;
            int mi = (time / 10000) % 100;
            int ss = (time / 100) % 100;
            int ms = time % 100;

            Calendar timestamp = Calendar.getInstance();
            timestamp.set(yyyy, mm, dd, hh24, mi, ss);
            return timestamp;
        }
        return null;
    }
}

