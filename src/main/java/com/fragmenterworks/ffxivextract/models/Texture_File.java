package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.ImageDecoding;
import com.fragmenterworks.ffxivextract.helpers.ImageDecoding.ImageDecodingException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public class Texture_File extends Game_File {

    final public int compressionType;
    final public int numMipMaps;
    final public int uncompressedWidth;
    final public int uncompressedHeight;
    final public int[] mipmapOffsets;
    final public byte[] data;

    public Texture_File(byte[] data, ByteOrder endian) {
        super(endian);

        this.data = data;

        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(endian);
        int attribute = bb.getInt(); // Uknown
        compressionType = bb.getInt();
        uncompressedWidth = bb.getShort();
        uncompressedHeight = bb.getShort();
        short depth = bb.getShort();
        numMipMaps = bb.getShort();
        mipmapOffsets = new int[numMipMaps];

        bb.position(0x1c);

        for (int i = 0; i < numMipMaps; i++)
            mipmapOffsets[i] = bb.getInt();
    }

    public final BufferedImage decode(int index, final Map<String, Object> parameters) throws ImageDecodingException {

        if (data == null)
            throw new NullPointerException("Data is null");

        int mipMapDivide = (int) (index == 0 ? 1 : Math.pow(2, index));

        switch (compressionType) {
            case 0x3420: {
                return ImageDecoding.decodeImageDX1(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide),
                        (uncompressedWidth / (mipMapDivide)) / 4,
                        (uncompressedHeight / (mipMapDivide)) / 4);
            }
            case 0x1130:
            case 0x1131: {
                return ImageDecoding.decodeImageRaw(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide), 0, 0);
            }
            case 0x3430: {
                return ImageDecoding.decodeImageDX3(data,
                        mipmapOffsets[index], uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide),
                        (uncompressedWidth / (mipMapDivide)) / 4,
                        (uncompressedHeight / (mipMapDivide)) / 4);
            }
            case 0x3431: {
                return ImageDecoding.decodeImageDX5(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide),
                        (uncompressedWidth / (mipMapDivide)) / 4,
                        (uncompressedHeight / (mipMapDivide)) / 4);
            }
            case 0x1440: {
                if (parameters != null) {
                    if (parameters.containsKey("4444.channel")) {
                        Object q = parameters.get("4444.channel");
                        return ImageDecoding.decodeImage4444split1channel(data,
                                mipmapOffsets[index],
                                uncompressedWidth / (mipMapDivide),
                                uncompressedHeight / (mipMapDivide), 0, 0,
                                (q instanceof Integer ? (Integer) q : 0));
                    }
                    if (parameters.containsKey("1008.4444.mergedSplit")
                            && parameters.get("1008.4444.mergedSplit").equals(
                            ImageDecoding.ON_VALUE)) {
                        return ImageDecoding.decodeImage4444split(data,
                                mipmapOffsets[index],
                                uncompressedWidth / (mipMapDivide),
                                uncompressedHeight / (mipMapDivide), 0, 0);
                    }
                }
                return ImageDecoding.decodeImage4444(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide), 0, 0);
            }
            case 0x1441: {
                return ImageDecoding.decodeImage5551(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide), 0, 0, parameters);
            }
            case 0x1450:
            case 0x1451: {
                return ImageDecoding.decodeImageRGBA(data,
                        mipmapOffsets[index],
                        uncompressedWidth / (mipMapDivide),
                        uncompressedHeight / (mipMapDivide), 0, 0);
            }
            case 0x2460: {
                return ImageDecoding.decodeImageRGBAF(data, endian, mipmapOffsets[index], uncompressedWidth / (mipMapDivide), uncompressedHeight / (mipMapDivide), 0, 0);
            }
        }
        throw new ImageDecodingException("Unsupported format: "
                + compressionType);
    }

    public byte[] getImage(String type) throws IOException, ImageDecodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(decode(0, null), "png", baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public String getCompressionTypeString() {
        switch (compressionType) {
            case 0x3420:
                return "DX1";
            case 0x3430:
                return "DX3";
            case 0x3431:
                return "DX5";
            case 0x1130:
            case 0x1131:
                return "RGB 8bit";
            case 0x1440:
                return "RGBA 4444";
            case 0x1441:
                return "RGBA 5551";
            case 0x1450:
            case 0x1451:
                return "RGBA";
            case 0x2460:
                return "RGBAF";
            default:
                return String.format("Unknown: 0x%x", compressionType);
        }
    }

    public int getSize() {
        if (compressionType == 0x3431) {
            return (uncompressedWidth * uncompressedWidth) / 2;
        }

        return -1;
    }

}
