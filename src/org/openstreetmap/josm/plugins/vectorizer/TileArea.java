package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.openstreetmap.gui.jmapviewer.Tile;

public class TileArea {

	public final Tile tile;

	public final boolean[] matrix;
	public final int width, height;
	public final Point coord;

	private final Point tileLeft, tileRight, tileUp, tileDown;

	public TileArea( Tile tile, boolean[] matrix ) {
		this.tile = tile;
		this.matrix = matrix;
		this.width = tile.getImage().getWidth();
		this.height = tile.getImage().getHeight();

		this.coord = new Point( tile.getXtile(), tile.getYtile() );
		this.tileLeft = new Point( tile.getXtile() - 1, tile.getYtile() );
		this.tileRight = new Point( tile.getXtile() + 1, tile.getYtile() );
		this.tileUp = new Point( tile.getXtile(), tile.getYtile() - 1 );
		this.tileDown = new Point( tile.getXtile(), tile.getYtile() + 1 );
	}

	public void paint() {
		int color = new Random().nextInt() % 255;

		BufferedImage img = tile.getImage();

		for ( int x = 0, ex = img.getWidth(); x < ex; ++x )
			for ( int y = 0, ey = img.getHeight(); y < ey; ++y )
				if ( matrix[x + y * ex] )
					img.setRGB( x, y, color );
	}

	public boolean contains( int x, int y, Area out ) {
		if ( x < 0 )
			return out.contains( tileLeft, x + tile.getImage().getWidth(), y );
		if ( x >= tile.getImage().getWidth() )
			return out.contains( tileRight, x - tile.getImage().getWidth(), y );

		if ( y < 0 )
			return out.contains( tileUp, x, y + tile.getImage().getHeight() );
		if ( y >= tile.getImage().getHeight() )
			return out.contains( tileDown, x, y - tile.getImage().getHeight() );

		return matrix[x + y * tile.getImage().getWidth()];
	}

}
