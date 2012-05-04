package org.openstreetmap.josm.plugins.vectorizer;

public interface ImageAccess {
	
	int getWidth();
	
	int getHeight();

	int getRGB(int x, int y);
	
}
