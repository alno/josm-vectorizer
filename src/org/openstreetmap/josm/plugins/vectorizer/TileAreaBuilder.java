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

	private TileAreaBuilder left, right, up, down;
	private boolean leftSet, rightSet, upSet, downSet;

	private final Point coord;
	private final int width, height;

	public TileAreaBuilder( AreaBuilder builder, Tile tile ) {
		this.builder = builder;
		this.tile = tile;
		this.colorSelector = builder.colorSelector;
		this.img = builder.task.createImageAccess( tile.getImage() );
		this.coord = new Point( tile.getXtile(), tile.getYtile() );
		this.width = tile.getImage().getWidth();
		this.height = tile.getImage().getHeight();

		this.matrix = new boolean[img.getWidth() * img.getHeight()];
	}

	public void enqueue( Point p ) {
		int x = p.x;
		int y = p.y;

		// XXX Ugly solution, but tiles may have different sizes, so we need to fix them o_O
		if ( x >= width ) {
			System.out.println( "Fixing x: " + x + " -> " + (width - 1) );
			x = width - 1;
		}

		if ( y >= height ) {
			System.out.println( "Fixing y: " + y + " -> " + (height - 1) );
			y = height - 1;
		}

		int ind = x + y * img.getWidth();

		if ( matrix[ind] )
			return;

		if ( !colorSelector.test( img.getRGB( x, y ) ) )
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
			else if ( left() != null )
				left().enqueue( new Point( left().width - 1, p.y ) );

			if ( p.x < width - 1 )
				enqueue( new Point( p.x + 1, p.y ) );
			else if ( right() != null )
				right().enqueue( new Point( 0, p.y ) );

			if ( p.y > 0 )
				enqueue( new Point( p.x, p.y - 1 ) );
			else if ( up() != null )
				up().enqueue( new Point( p.x, up().height - 1 ) );

			if ( p.y < height - 1 )
				enqueue( new Point( p.x, p.y + 1 ) );
			else if ( down() != null )
				down().enqueue( new Point( p.x, 0 ) );
		}
	}

	public TileArea result() {
		return new TileArea( tile, matrix );
	}

	protected TileAreaBuilder left() {
		if ( !leftSet ) {
			left = builder.getTileBuilder( new Point( coord.x - 1, coord.y ) );
			leftSet = true;
		}

		return left;
	}

	protected TileAreaBuilder right() {
		if ( !rightSet ) {
			right = builder.getTileBuilder( new Point( coord.x + 1, coord.y ) );
			rightSet = true;
		}

		return right;
	}

	protected TileAreaBuilder up() {
		if ( !upSet ) {
			up = builder.getTileBuilder( new Point( coord.x, coord.y - 1 ) );
			upSet = true;
		}

		return up;
	}

	protected TileAreaBuilder down() {
		if ( !downSet ) {
			down = builder.getTileBuilder( new Point( coord.x, coord.y + 1 ) );
			downSet = true;
		}

		return down;
	}

}
