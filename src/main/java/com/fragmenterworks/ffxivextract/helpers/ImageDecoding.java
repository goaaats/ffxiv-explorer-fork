package com.fragmenterworks.ffxivextract.helpers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

@SuppressWarnings("unused")
public final class ImageDecoding {

	public static class ImageDecodingException extends Exception {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public ImageDecodingException(final String message) {
			super(message);
		}
	}

	public static final String GRAYSCALE_TRANSPARENCY_D5551_KEY = "5551-transp";

	public static final Boolean ON_VALUE = Boolean.TRUE;
	public static final Boolean OFF_VALUE = Boolean.FALSE;

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImage4444(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (targetHeight * targetWidth * 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {
				final int pixel = buffer.getShort() & 0xffff;
				final int r = ((pixel & 0xF)) * 16;
				final int g = ((pixel & 0xF0) >> 4) * 16;
				final int b = ((pixel & 0xF00) >> 8) * 16;
				final int a = ((pixel & 0xF000) >> 12) * 16;
				p += 2;
				img.setRGB(x, y, new Color(b, g, r, a).getRGB());
			}
		}
		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImage4444split(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth * 2, targetHeight * 2, BufferedImage.TYPE_BYTE_INDEXED);
		if (data.length < (targetHeight * targetWidth * 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {
				final int pixel = buffer.getShort() & 0xffff;
				final int r = 255 - ((pixel & 0xF)) * 16;
				final int g = 255 - ((pixel & 0xF0) >> 4) * 16;
				final int b = 255 - ((pixel & 0xF00) >> 8) * 16;
				final int a = 255 - ((pixel & 0xF000) >> 12) * 16;
				p += 2;
				img.setRGB(x, y, new Color(b, b, b).getRGB());
				img.setRGB(x + targetWidth, y, new Color(g, g, g).getRGB());
				img.setRGB(x, y + targetHeight, new Color(r, r, r).getRGB());
				img.setRGB(x + targetWidth, y + targetHeight, new Color(a, a, a).getRGB());
			}
		}
		return img;
	}

	public static BufferedImage decodeImage4444split1channel(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight, int channel) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
		if (data.length < (targetHeight * targetWidth * 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		final int r = 0;
		final int g = 0;
		final int b = 0;
		switch (channel) {
			case 0: { //red
				for (int y = 0; y < targetHeight; y++) {
					for (int x = 0; x < targetWidth; x++) {
						final int pixel = buffer.getShort() & 0xffff;
						final int v = ((pixel & 0xF)) * 16;
						img.setRGB(x, y, new Color(r, g, b, v).getRGB());
						p += 2;
					}
				}
				break;
			}
			case 1: { //green
				for (int y = 0; y < targetHeight; y++) {
					for (int x = 0; x < targetWidth; x++) {
						final int pixel = buffer.getShort() & 0xffff;
						final int v = ((pixel & 0xF0) >> 4) * 16;
						img.setRGB(x, y, new Color(r, g, b, v).getRGB());
						p += 2;
					}
				}
				break;
			}
			case 2: { //blue
				for (int y = 0; y < targetHeight; y++) {
					for (int x = 0; x < targetWidth; x++) {
						final int pixel = buffer.getShort() & 0xffff;
						final int v = ((pixel & 0xF00) >> 8) * 16;
						img.setRGB(x, y, new Color(r, g, b, v).getRGB());
						p += 2;
					}
				}
				break;
			}
			case 3: { //alpha
				for (int y = 0; y < targetHeight; y++) {
					for (int x = 0; x < targetWidth; x++) {
						final int pixel = buffer.getShort() & 0xffff;
						final int v = ((pixel & 0xF000) >> 12) * 16;
						img.setRGB(x, y, new Color(r, g, b, v).getRGB());
						p += 2;
					}
				}
				break;
			}
		}
		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @param parameters
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImage5551(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight, final Map<String, Object> parameters) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (targetHeight * targetWidth * 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {
				final int pixel = buffer.getShort() & 0xffff;

				final int r, g, b;
				int a;
				if ((parameters != null) && parameters.containsKey(GRAYSCALE_TRANSPARENCY_D5551_KEY) && parameters.get(
						GRAYSCALE_TRANSPARENCY_D5551_KEY
				).equals(ON_VALUE)) {
					b = ((pixel & 0x1F)) * 8;
					g = ((pixel & 0x3E0) >> 5) * 8;
					r = ((pixel & 0x7E00) >> 10) * 8;
					a = ((pixel & 0x8000) >> 15) * 255;
					a = Math.min(255, (r + g + b) / 3);
				} else {
					b = (pixel & 0x1F) * 8;
					g = ((pixel & 0x3E0) >> 5) * 8;
					r = ((pixel & 0x7E00) >> 10) * 8;
					a = ((pixel & 0x8000) >> 15) * 255;
					/*
					 * if (a == 255) { final int t = 60; if ((r < t) && (g < t)
					 * && (b < t)) { a = Math.min(255, (r + g + b) / 3); } }
					 */
				}
				p += 4;
				img.setRGB(x, y, new Color(r, g, b, a).getRGB());
			}
		}
		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImageDX1(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < ((targetHeight * targetWidth) / 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < compressedHeight; y++) {
			for (int x = 0; x < compressedWidth; x++) {
				final int t0 = buffer.getShort() & 0xffff;
				final int t1 = buffer.getShort() & 0xffff;
				final int t2 = buffer.getShort() & 0xffff;
				final int t3 = buffer.getShort() & 0xffff;

				p += 8;
				decompressBlockDTX1(x * 4, y * 4, targetWidth, t0, t1, t2, t3, img);
			}
		}
		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImageDX3(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < ((targetHeight * targetWidth) / 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < compressedHeight; y++) {
			for (int x = 0; x < compressedWidth; x++) {
				final int t0 = buffer.getShort() & 0xffff;
				final int t1 = buffer.getShort() & 0xffff;
				final int t2 = buffer.getShort() & 0xffff;
				final int t3 = buffer.getShort() & 0xffff;

				p += 8;
				decompressBlockDTX1(x * 4, y * 4, targetWidth, t0, t1, t2, t3, img);
			}
		}
		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImageDX5(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < ((targetHeight * targetWidth) / 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < compressedHeight; y++) {
			for (int x = 0; x < compressedWidth; x++) {
				// $types = array("B", "B", "S", "S", "S", "S", "S", "S", "S");
				final int t0 = buffer.get() & 0xff;
				final int t1 = buffer.get() & 0xff;

				final int t2 = buffer.getShort() & 0xffff;
				final int t3 = buffer.getShort() & 0xffff;
				final int t4 = buffer.getShort() & 0xffff;
				final int t5 = buffer.getShort() & 0xffff;
				final int t6 = buffer.getShort() & 0xffff;
				final int t7 = buffer.getShort() & 0xffff;
				final int t8 = buffer.getShort() & 0xffff;

				p += 8;
				decompressBlockDTX5(x * 4, y * 4, targetWidth, t0, t1, t2, t3, t4, t5, t6, t7, t8, img);
			}
		}
		return img;
	}

	public static BufferedImage decodeImageRaw(final byte[] data, final int offset, final int uncompressedWidth, final int uncompressedHeight, final int i, final int j) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(uncompressedWidth, uncompressedHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (uncompressedWidth * uncompressedHeight)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < uncompressedHeight; y++) {
			for (int x = 0; x < uncompressedWidth; x++) {
				final int pixel = buffer.get() & 0xff;

				final int r, g, b;
				int a;
				b = pixel;
				g = pixel;
				r = pixel;
				a = 255;
				p += 1;
				img.setRGB(x, y, new Color(r, g, b, a).getRGB());
			}
		}
		return img;
	}

	public static BufferedImage decodeImageRGBA(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		return decodeImageRGBA(data, offset, targetWidth, targetHeight, compressedWidth, compressedHeight, ByteOrder.LITTLE_ENDIAN);
	}

	public static BufferedImage decodeImageRGBA(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight, ByteOrder order) throws ImageDecodingException {
		System.out.printf("width: %d height: %d, twidth: %d theight: %d\n", compressedWidth, compressedHeight, targetWidth, targetHeight);

		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (targetHeight * targetWidth * 4)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);


		int p = 0;

		if (order == ByteOrder.LITTLE_ENDIAN) {
			for (int y = 0; y < targetHeight; y++) {
				for (int x = 0; x < targetWidth; x++) {
					final int b = buffer.get() & 0xff;
					final int g = buffer.get() & 0xff;
					final int r = buffer.get() & 0xff;
					final int a = buffer.get() & 0xff;
					p += 4;
					img.setRGB(x, y, new Color(r, g, b, a).getRGB());
				}
			}
		} else {
			for (int x = 0; x < targetWidth; x++) {
				for (int y = 0; y < targetHeight; y++) {
					final int a = buffer.get() & 0xff;
					final int r = buffer.get() & 0xff;
					final int g = buffer.get() & 0xff;
					final int b = buffer.get() & 0xff;
					p += 4;
					img.setRGB(x, y, new Color(r, g, b, a).getRGB());
				}
			}

		}


		return img;
	}

	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImageRGBAF(final byte[] data, ByteOrder endian, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (targetHeight * targetWidth * 8)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(endian);
		buffer.position(offset);

		int p = 0;
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {

				// this may need to be reversed with endian
				float fr = Utils.convertHalfToFloat(buffer.getShort());
				float fg = Utils.convertHalfToFloat(buffer.getShort());
				float fb = Utils.convertHalfToFloat(buffer.getShort());
				float fa = Utils.convertHalfToFloat(buffer.getShort());

				if (fr > 1)
					fr = 1.0f;
				if (fg > 1)
					fg = 1.0f;
				if (fb > 1)
					fb = 1.0f;
				if (fa > 1)
					fa = 1.0f;
				int b = (int) (255f * fb);
				int g = (int) (255f * fg);
				int r = (int) (255f * fr);
				int a = (int) (255f * fa);

				if (r > 255)
					r = 255;
				if (g > 255)
					g = 255;
				if (b > 255)
					b = 255;
				if (a > 255)
					a = 255;
				p += 4;
				img.setRGB(x, y, new Color(r, g, b, a).getRGB());
			}
		}
		return img;
	}


	/**
	 * @param data
	 * @param targetWidth
	 * @param targetHeight
	 * @param compressedWidth
	 * @param compressedHeight
	 * @return
	 * @throws ImageDecodingException
	 */
	public static BufferedImage decodeImageTester(final byte[] data, final int offset, final int targetWidth, final int targetHeight, final int compressedWidth, final int compressedHeight) throws ImageDecodingException {
		final BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		if (data.length < (targetHeight * targetWidth * 2)) {
			throw new ImageDecodingException("Data too short");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(offset);

		final int p = 0;
		for (int y = 0; y < targetHeight; y++) {
			for (int x = 0; x < targetWidth; x++) {

			}
		}
		return img;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param color0
	 * @param color1
	 * @param txl1
	 * @param txl2
	 * @param img
	 */
	private static void decompressBlockDTX1(final int x, final int y, final int width, final int color0, final int color1, final int txl1, final int txl2, final BufferedImage img) {
		float temp = ((color0 >> 11) * 255f) + 16f;
		final float r0 = (((temp / 32f) + temp) / 32f);
		temp = (((color0 & 0x07E0) >> 5) * 255f) + 32f;
		final float g0 = (((temp / 64f) + temp) / 64f);
		temp = ((color0 & 0x001F) * 255f) + 16f;
		final float b0 = (((temp / 32f) + temp) / 32f);

		temp = ((color1 >> 11) * 255f) + 16f;
		final float r1 = (((temp / 32f) + temp) / 32f);
		temp = (((color1 & 0x07E0) >> 5) * 255f) + 32f;
		final float g1 = (((temp / 64f) + temp) / 64f);
		temp = ((color1 & 0x001F) * 255f) + 16f;
		final float b1 = (((temp / 32f) + temp) / 32f);


		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				// final Color finalColor;
				final int d = (4 * j) + i;
				int positionCode;
				if ((d * 2) >= 16) {
					positionCode = (txl2 >> ((d * 2) % 16)) & 0x03;
				} else {
					positionCode = (txl1 >> (d * 2)) & 0x03;
				}
				float fr, fg, fb, fa;
				if (color0 > color1) {
					switch (positionCode) {
						case 0: {
							fr = r0;
							fg = g0;
							fb = b0;
							fa = 0;
							break;
						}
						case 1: {
							fr = r1;
							fg = g1;
							fb = b1;
							fa = 0;
							break;
						}
						case 2: {
							fr = ((2f * r0) + r1) / 3f;
							fg = ((2f * g0) + g1) / 3f;
							fb = ((2f * b0) + b1) / 3f;
							fa = 0;
							break;
						}
						case 3: {
							fr = (r0 + (2f * r1)) / 3f;
							fg = (g0 + (2f * g1)) / 3f;
							fb = (b0 + (2f * b1)) / 3f;
							fa = 0;
							break;
						}
						default: {
							fr = 0;
							fg = 0;
							fb = 0;
							fa = 0;
						}
					}
				} else {
					switch (positionCode) {
						case 0: {
							fr = r0;
							fg = g0;
							fb = b0;
							fa = 0xff;
							break;
						}
						case 1: {
							fr = r1;
							fg = g1;
							fb = b1;
							fa = 0xff;
							break;
						}
						case 2: {
							fr = (r0 + r1) / 2f;
							fg = (g0 + g1) / 2f;
							fb = (b0 + b1) / 2f;
							fa = 0xff;
							break;
						}
						case 3: {
							fr = 0;
							fg = 0;
							fb = 0;
							fa = 0xff;
							break;
						}
						default: {
							fr = 0;
							fg = 0;
							fb = 0;
							fa = 0;
						}
					}
				}

				if ((x + i) < width) {
					int alpha = 0;
					if ((fr == fg) && (fr == fb) && (fr == 0)) {
						alpha = 0xff;
					}
					img.setRGB(x + i, y + j, new Color((int) fr, (int) fg, (int) fb, 255 - alpha).getRGB());
				}
			}
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param bit0
	 * @param bit1
	 * @param bit2
	 * @param bit3
	 * @param bit4
	 * @param color0
	 * @param color1
	 * @param txl1
	 * @param txl2
	 * @param img
	 */
	private static void decompressBlockDTX5(final int x, final int y, final int width, final int bit0, final int bit1, final int bit2, final int bit3, final int bit4, final int color0, final int color1, final int txl1, final int txl2, final BufferedImage img) {
		final int alpha0 = bit0;
		final int alpha1 = bit1;
		final int alphaCode1 = (bit4 << 8) | ((bit3 >> 8) & 0xff);
		final int alphaCode2 = ((bit3 & 0xff) << 16) | bit2;

		int temp = ((color0 >> 11) * 255) + 16;
		final int r0 = (((temp / 32) + temp) / 32);
		temp = (((color0 & 0x07E0) >> 5) * 255) + 32;
		final int g0 = (((temp / 64) + temp) / 64);
		temp = ((color0 & 0x001F) * 255) + 16;
		final int b0 = (((temp / 32) + temp) / 32);

		temp = ((color1 >> 11) * 255) + 16;
		final int r1 = (((temp / 32) + temp) / 32);
		temp = (((color1 & 0x07E0) >> 5) * 255) + 32;
		final int g1 = (((temp / 64) + temp) / 64);
		temp = ((color1 & 0x001F) * 255) + 16;
		final int b1 = (((temp / 32) + temp) / 32);

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {

				int d = 3 * ((4 * j) + i);
				int alphaCode;
				if (d < 24) {
					alphaCode = (alphaCode2 >> d) & 0x07;
				} else {
					alphaCode = (alphaCode1 >> (d - 24)) & 0x07;
				}

				int finalAlpha = 0;
				if (alphaCode == 0) {
					finalAlpha = alpha0;
				} else if (alphaCode == 1) {
					finalAlpha = alpha1;
				} else {
					if (alpha0 > alpha1) {
						finalAlpha = (((8 - alphaCode) * alpha0) + ((alphaCode - 1) * alpha1)) / 7;
					} else {
						if (alphaCode == 6) {
							finalAlpha = 0;
						} else if (alphaCode == 7) {
							finalAlpha = 255;
						} else {
							finalAlpha = (((6 - alphaCode) * alpha0) + ((alphaCode - 1) * alpha1)) / 5;
						}
					}
				}

				d = (4 * j) + i;
				int colorCode;
				if ((d * 2) >= 16) {
					colorCode = (txl2 >> ((d * 2) % 16)) & 0x03;
				} else {
					colorCode = (txl1 >> (d * 2)) & 0x03;
				}
				final Color finalColor;
				switch (colorCode) {
					case 0: {
						finalColor = new Color(r0, g0, b0, finalAlpha);
						break;
					}
					case 1: {
						finalColor = new Color(r1, g1, b1, finalAlpha);
						break;
					}
					case 2: {
						finalColor = new Color(
								((2 * r0) + r1) / 3, ((2 * g0) + g1) / 3, ((2 * b0) + b1) / 3, finalAlpha
						);
						break;
					}
					case 3: {
						finalColor = new Color(
								(r0 + (2 * r1)) / 3, (g0 + (2 * g1)) / 3, (b0 + (2 * b1)) / 3, finalAlpha
						);
						break;
					}
					default: {
						finalColor = new Color(0, 0, 0);
					}
				}

				if ((x + i) < width) {
					img.setRGB(x + i, y + j, finalColor.getRGB());
				}
			}
		}
	}

	private ImageDecoding() throws InstantiationException {
		throw new InstantiationException();
	}

}
