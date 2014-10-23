package ca.fraggergames.ffxivextract.models;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Map;

import javax.imageio.ImageIO;

import ca.fraggergames.ffxivextract.helpers.ImageDecoding;
import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;

public class Texture_File {

	final public int compressionType;

	final public int numMipMaps;

	final public int dataStart[];

	final public int uncompressedWidth;
	final public int uncompressedHeight;

	final public byte data[];

	final public short numFrames;
	
	public Texture_File(byte data[]) {

		this.data = data;

		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.getInt(); // Uknown
		compressionType = bb.get();
		numMipMaps = bb.get();		
		bb.getShort();
		uncompressedWidth = bb.getShort();
		uncompressedHeight = bb.getShort();
		bb.getShort();
		numFrames = bb.getShort();
		
		bb.position(0x1c);
		
		dataStart = new int[numFrames];		
		for (int i = 0; i < numFrames; i++)
			dataStart[i] = bb.getInt(); 
	}

	public final BufferedImage decode(int index, 
			final Map<String, Object> parameters) throws ImageDecodingException {

		if (data == null) {
			throw new NullPointerException("Data is null");
		}
		switch (compressionType) {
		case 32: {
			return ImageDecoding.decodeImageDX1(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight,
					uncompressedWidth / 4,
					uncompressedHeight / 4);
		}
		case 48: {
			return ImageDecoding.decodeImageRaw(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight, 0, 0);
		}
		case 49: {
			return ImageDecoding.decodeImageDX5(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight,
					uncompressedWidth / 4,
					uncompressedHeight / 4);
		}
		case 64: {
			if (parameters != null) {
				if (parameters.containsKey("4444.channel")) {
					Object q = parameters.get("4444.channel");
					return ImageDecoding.decodeImage4444split1channel(data,
							dataStart[index],
							uncompressedWidth,
							uncompressedHeight, 0, 0,
							(q instanceof Integer ? (Integer) q : 0));
				}
				if (parameters.containsKey("1008.4444.mergedSplit")
						&& parameters.get("1008.4444.mergedSplit").equals(
								ImageDecoding.ON_VALUE)) {
					return ImageDecoding.decodeImage4444split(data,
							dataStart[index],
							uncompressedWidth,
							uncompressedHeight, 0, 0);
				}
			}
			return ImageDecoding.decodeImage4444(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight, 0, 0);
		}
		case 65: {
			return ImageDecoding.decodeImage5551(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight, 0, 0, parameters);
		}
		case 80: {
			return ImageDecoding.decodeImageRGBA(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight, 0, 0);
		}
		case 81: {
			return ImageDecoding.decodeImageRGBA(data,
					dataStart[index],
					uncompressedWidth,
					uncompressedHeight, 0, 0);
		}
		}
		throw new ImageDecodingException("Unsupported format: "
				+ compressionType);
	}

	public byte[] getImage(String type) throws IOException, ImageDecodingException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(decode(0, null), "png", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;		
	}
	
}
