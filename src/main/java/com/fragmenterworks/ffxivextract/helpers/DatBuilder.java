package com.fragmenterworks.ffxivextract.helpers;

import com.jcraft.jzlib.GZIPOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

public class DatBuilder {

    private EARandomAccessFile fileOut = null;
    private final String path;
    private long currentOffset = 0x800;
    private final int index;
    private final ByteOrder endian;

    public DatBuilder(int index, String outPath, ByteOrder endian) throws FileNotFoundException {
        this.endian = endian;
        path = outPath;
        this.index = index + 1;

        File f = new File(outPath);
        if (f.exists())
            f.delete();

        fileOut = new EARandomAccessFile(new File(outPath), "rw", endian);
    }

    public long addFile(String path) throws IOException {
        File fileToLoad = new File(path);
        byte[] data = new byte[(int) fileToLoad.length()];

        FileInputStream fIn = new FileInputStream(fileToLoad);
        fIn.read(data);
        fIn.close();

        ArrayList<DataBlock> blocks = new ArrayList<DatBuilder.DataBlock>();
        int position = 0;
        int largestBlock = 0;
        while (data.length - position > 16000) {
            byte[] uncompressedBlock = new byte[16000];
            System.arraycopy(data, position, uncompressedBlock, 0, 16000);
            DataBlock currentBlock = null;
            try {
                currentBlock = new DataBlock(blocks.size() == 0 ? 0 : blocks.get(blocks.size() - 1).nextOffset, uncompressedBlock);
                blocks.add(currentBlock);
                position += 16000;
            } catch (IOException e) {
                Utils.getGlobalLogger().error("", e);
            }

            if (currentBlock.uncompressedSize > largestBlock)
                largestBlock = currentBlock.uncompressedSize;
        }

        if (data.length - position > 0) {
            byte[] finalBlock = new byte[data.length - position - 1];
            System.arraycopy(data, position, finalBlock, 0, finalBlock.length);
            try {
                DataBlock currentBlock = new DataBlock(blocks.size() == 0 ? 0 : blocks.get(blocks.size() - 1).nextOffset, finalBlock);
                blocks.add(currentBlock);

                if (currentBlock.uncompressedSize > largestBlock)
                    largestBlock = currentBlock.uncompressedSize;
            } catch (Exception e) {
                return -1;
            }
        }

        //Generate Header
        byte[] tempHeader = new byte[0x9900];
        ByteBuffer headerBB = ByteBuffer.wrap(tempHeader);
        headerBB.order(endian);
        headerBB.putInt(0x100);
        headerBB.putInt(2);
        headerBB.putInt(data.length);
        headerBB.putInt(0);
        headerBB.putInt(largestBlock * 2);
        headerBB.putInt(blocks.size());

        Iterator<DataBlock> it = blocks.iterator();
        int headerSize = 0;
        while (it.hasNext()) {
            DataBlock block = it.next();
            headerBB.putInt(block.offset); //Offset
            headerBB.putShort(block.totalSize);
            headerBB.putShort((short) block.uncompressedSize);
        }
        headerSize += (6 * 4) + (3 * 4 * blocks.size());
        while ((headerSize & 0xFF) != 0x00)
            headerSize += 1;

        headerBB.rewind();
        headerBB.putInt(headerSize);
        byte[] header = new byte[headerSize];
        System.arraycopy(tempHeader, 0, header, 0, header.length);

        fileOut.seek(currentOffset);
        fileOut.write(header);

        Iterator<DataBlock> it2 = blocks.iterator();
        while (it2.hasNext()) {
            DataBlock block = it2.next();

            fileOut.seek(currentOffset + header.length + block.offset);

            byte[] data2 = new byte[16 + block.compressedSize];
            ByteBuffer bb = ByteBuffer.wrap(data2);
            bb.order(endian);

            //Write Header
            bb.putInt(0x10);
            bb.putInt(0x0);
            bb.putInt(block.compressedSize);
            bb.putInt(block.uncompressedSize);

            //Write Block
            bb.put(block.compressedData);

            bb.rewind();

            fileOut.write(data2);
        }

        long curposition = fileOut.getFilePointer();
        while ((curposition & 0xFF) != 0x00)
            curposition += 1;

        long entryOffset = ((currentOffset) / 8) + ((index - 1) * 2);
        currentOffset = curposition;

        Utils.getGlobalLogger().debug("Added file at path {} to offset {}", path, String.format("0x%08X", entryOffset));

        return entryOffset;
    }

    private byte[] buildSqpackDatHeader() {
        byte[] sqpackHeader = new byte[0x400];

        try {
            ByteBuffer sqpackHeaderBB = ByteBuffer.wrap(sqpackHeader);
            sqpackHeaderBB.order(endian);

            int signature = 0x61507153;
            int signature2 = 0x00006b63;

            sqpackHeaderBB.putInt(signature);
            sqpackHeaderBB.putInt(signature2);
            sqpackHeaderBB.putInt(0);
            sqpackHeaderBB.putInt(0x400);
            sqpackHeaderBB.putInt(1);
            sqpackHeaderBB.putInt(1);
            sqpackHeaderBB.position(0x20);
            sqpackHeaderBB.putInt(0xFFFFFFFF);

            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(sqpackHeader, 0, 0x3BF);
            sqpackHeaderBB.position(0x3c0);
            sqpackHeaderBB.put(md.digest());

        } catch (NoSuchAlgorithmException e) {
            Utils.getGlobalLogger().error("", e);
            return null;
        }

        return sqpackHeader;
    }

    private byte[] buildSqpackDatDataHeader(int datBodyLength, byte[] datBodySha1, int spannedIndexNum) {
        byte[] dataHeader = new byte[0x400];

        try {
            ByteBuffer dataHeaderBB = ByteBuffer.wrap(dataHeader);
            dataHeaderBB.order(endian);

            dataHeaderBB.putInt(dataHeader.length);
            dataHeaderBB.putInt(0);
            dataHeaderBB.putInt(0x10);
            dataHeaderBB.putInt(datBodyLength);
            dataHeaderBB.putInt(spannedIndexNum);
            dataHeaderBB.putInt(0);
            dataHeaderBB.putInt(0x77359400);
            dataHeaderBB.putInt(0);

            dataHeaderBB.put(datBodySha1);

            //Sha1 of header
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(dataHeader, 0, 0x3BF);
            dataHeaderBB.position(0x3C0);
            dataHeaderBB.put(md.digest());

        } catch (NoSuchAlgorithmException e) {
            Utils.getGlobalLogger().error("", e);
            return null;
        }

        return dataHeader;
    }

    private class DataBlock {
        final short totalSize;
        final byte[] compressedData;
        final int compressedSize;
        final int uncompressedSize;
        final int offset;
        int nextOffset;

        DataBlock(int offset, byte[] toBeCompressed) throws IOException {
            this.offset = offset;
            uncompressedSize = toBeCompressed.length;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream(toBeCompressed.length);
            GZIPOutputStream zipOut = new GZIPOutputStream(byteOut);
            zipOut.write(toBeCompressed);
            zipOut.finish();
            compressedSize = (int) (zipOut.getTotalOut() - 10);
            zipOut.close();
            byte[] tempDecompressed = byteOut.toByteArray();
            compressedData = new byte[compressedSize];
            System.arraycopy(tempDecompressed, 10, compressedData, 0, compressedSize);

            //Generate offset
            nextOffset = compressedSize + 0x10 + offset;
            while ((nextOffset & 0xFF) != 0x00)
                nextOffset += 1;

            totalSize = (short) (nextOffset - offset);
        }
    }

    public void finish() {

        try {
            byte[] buffer = new byte[2048];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            fileOut.seek(0x800);
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                Utils.getGlobalLogger().error("", e);
            }
            while (true) {
                int bytesRead = fileOut.read(buffer);
                bb.rewind();
                if (bytesRead <= 0)
                    break;
                md.update(buffer, 0, bytesRead);
                bb.rewind();
            }

            byte[] sha1 = md.digest();

            fileOut.seek(0);
            fileOut.write(buildSqpackDatHeader());
            fileOut.write(buildSqpackDatDataHeader((int) currentOffset, sha1, index));
            fileOut.close();

            Utils.getGlobalLogger().info("Dat file created at {}!", path);
        } catch (IOException e) {
            Utils.getGlobalLogger().error("Unable to create DAT file.", e);
        } finally {
            try {
                fileOut.close();
            } catch (IOException e) {
                Utils.getGlobalLogger().error("", e);
            }
        }
    }
}
