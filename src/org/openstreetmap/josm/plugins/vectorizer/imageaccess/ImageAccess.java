package org.openstreetmap.josm.plugins.vectorizer.imageaccess;

public interface ImageAccess {
	
	int getWidth();
	
	int getHeight();

	int getRGB(int x, int y);
	
}
