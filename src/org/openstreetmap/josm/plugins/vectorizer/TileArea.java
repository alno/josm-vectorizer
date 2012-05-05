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

	private TileArea left, right, up, down;
	private boolean leftSet, rightSet, upSet, downSet;

	public TileArea( Tile tile, boolean[] matrix ) {
		this.tile = tile;
		this.matrix = matrix;
		this.width = tile.getImage().getWidth();
		this.height = tile.getImage().getHeight();
		this.coord = new Point( tile.getXtile(), tile.getYtile() );
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
			return left( out ) != null && left( out ).contains( x + left( out ).width, y, out );
		if ( x >= tile.getImage().getWidth() )
			return right( out ) != null && right( out ).contains( x - width, y, out );

		if ( y < 0 )
			return up( out ) != null && up( out ).contains( x, y + up( out ).height, out );

		if ( y >= tile.getImage().getHeight() )
			return down( out ) != null && down( out ).contains( x, y - height, out );

		return matrix[x + y * tile.getImage().getWidth()];
	}

	protected TileArea left( Area out ) {
		if ( !leftSet ) {
			left = out.getTileArea( new Point( coord.x - 1, coord.y ) );
			leftSet = true;
		}

		return left;
	}

	protected TileArea right( Area out ) {
		if ( !rightSet ) {
			right = out.getTileArea( new Point( coord.x + 1, coord.y ) );
			rightSet = true;
		}

		return right;
	}

	protected TileArea up( Area out ) {
		if ( !upSet ) {
			up = out.getTileArea( new Point( coord.x, coord.y - 1 ) );
			upSet = true;
		}

		return up;
	}

	protected TileArea down( Area out ) {
		if ( !downSet ) {
			down = out.getTileArea( new Point( coord.x, coord.y + 1 ) );
			downSet = true;
		}

		return down;
	}

}
