package com.fragmenterworks.ffxivextract.models;

import java.awt.image.BufferedImage;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class TextureRegion {
	private final BufferedImage bufferedImage;
	public final int x;
	public final int y;
	public final int w;
	public final int h;

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public BufferedImage getImage() {
		return bufferedImage;
	}

	public TextureRegion(final BufferedImage bufferedImage, final int x, final int y, final int w, final int h) {

		this.bufferedImage = bufferedImage;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;


	}


}
