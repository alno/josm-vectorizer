package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Queue;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.josm.plugins.vectorizer.imageaccess.ImageAccess;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;

public class TileAreaBuilder {

	private final AreaBuilder builder;
	private final ColorSelector colorSelector;

	private final Tile tile;
	private final ImageAccess img;

	private final boolean[] matrix;

	private final Queue<Point> queue = new ArrayDeque<Point>();

	private final Point tileLeft, tileRight, tileUp, tileDown;

	public TileAreaBuilder( AreaBuilder builder, Tile tile ) {
		this.builder = builder;
		this.tile = tile;
		this.colorSelector = builder.colorSelector;
		this.img = builder.task.createImageAccess( tile.getImage() );

		this.matrix = new boolean[img.getWidth() * img.getHeight()];

		this.tileLeft = new Point( tile.getXtile() - 1, tile.getYtile() );
		this.tileRight = new Point( tile.getXtile() + 1, tile.getYtile() );
		this.tileUp = new Point( tile.getXtile(), tile.getYtile() - 1 );
		this.tileDown = new Point( tile.getXtile(), tile.getYtile() + 1 );
	}

	public void enqueue( Point p ) {
		int ind = p.x + p.y * img.getWidth();

		if ( matrix[ind] )
			return;

		if ( !colorSelector.test( img.getRGB( p.x, p.y ) ) )
			return;

		matrix[ind] = true;
		queue.add( p );
	}

	public boolean queued() {
		return !queue.isEmpty();
	}

	public void process() {
		while ( !queue.isEmpty() ) {
			Point p = queue.poll();

			if ( p.x > 0 )
				enqueue( new Point( p.x - 1, p.y ) );
			else
				builder.enqueue( tileLeft, new Point( img.getWidth() - 1, p.y ) );

			if ( p.x < img.getWidth() - 1 )
				enqueue( new Point( p.x + 1, p.y ) );
			else
				builder.enqueue( tileRight, new Point( 0, p.y ) );

			if ( p.y > 0 )
				enqueue( new Point( p.x, p.y - 1 ) );
			else
				builder.enqueue( tileUp, new Point( p.x, img.getHeight() - 1 ) );

			if ( p.y < img.getHeight() - 1 )
				enqueue( new Point( p.x, p.y + 1 ) );
			else
				builder.enqueue( tileDown, new Point( p.x, 0 ) );
		}
	}

	public TileArea result() {
		return new TileArea( tile, matrix );
	}

}
