package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

public class IMC_File extends Game_File {

    private int numVariances;

    final HashMap<Integer, ImcPart> parts = new HashMap<Integer, ImcPart>();

    public IMC_File(String path, ByteOrder endian) throws IOException {
        super(endian);
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        loadIMC(data);
    }

    public IMC_File(byte[] data, ByteOrder endian) {
        super(endian);
        loadIMC(data);
    }

    private void loadIMC(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(endian);

        numVariances = bb.getShort();
        int partMask = bb.getShort();
        boolean gotFirst = false;

        //This is weird variants sitting here. SaintCoinach reads it based on the part mask.
        for (int i = 0; i < 8; i++) {
            int bit = (byte) (1 << i);
            if ((partMask & bit) == bit) {
                if (!gotFirst) {
                    gotFirst = true;
                }
                parts.put(i, new ImcPart(bit, new VarianceInfo(bb.getShort(), bb.getShort(), bb.getShort())));
            }
        }

        //Get the variances
        int remaining = numVariances;
        while (--remaining >= 0) {
            for (ImcPart imcPart : parts.values())
                imcPart.variants.add(new VarianceInfo(bb.getShort(), bb.getShort(), bb.getShort()));
        }
    }

    public VarianceInfo getVarianceInfo(int i) {
        if (i > numVariances || i == -1)
            return parts.get(0).variants.get(0);

        return parts.get(0).variants.get(i);
    }

    public ArrayList<VarianceInfo> getVariantsList(int key) {
        return parts.get(key).variants;
    }

    public int getNumVariances() {
        return numVariances;
    }

    public static class ImcPart {
        final int bit;
        final VarianceInfo partsVarient;
        public final ArrayList<VarianceInfo> variants = new ArrayList<VarianceInfo>();

        ImcPart(int bit, VarianceInfo variance) {
            this.bit = bit;
            partsVarient = variance;
        }
    }

    public static class VarianceInfo {
        public final short materialNumber;
        public final short partVisibiltyMask;
        final short effectNumber;

        VarianceInfo(short materialNumber, short partVisiMask, short effectNumber) {
            this.materialNumber = materialNumber == 0 ? 1 : materialNumber;
            this.partVisibiltyMask = partVisiMask;
            this.effectNumber = effectNumber;
        }

        @Override
        public String toString() {
            return String.format("Mat#: %d, Parts:0x%x, Eff#: %d", materialNumber, partVisibiltyMask, effectNumber);
        }
    }
}
