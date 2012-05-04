package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.image.BufferedImage;

public class DirectImageAccess implements ImageAccess {
	
	private final BufferedImage img;

	public DirectImageAccess( BufferedImage img ) {
		this.img = img;
	}

	public int getWidth() {
		return img.getWidth();
	}

	public int getHeight() {
		return img.getHeight();
	}

	public int getRGB( int x, int y ) {
		return img.getRGB(x, y);
	}
	
	

}
