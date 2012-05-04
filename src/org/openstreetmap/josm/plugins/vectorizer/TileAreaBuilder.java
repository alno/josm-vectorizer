package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;

public class TileAreaBuilder {

	private final ColorSelector colorSelector;

	private final Tile tile;
	private final ImageAccess img;

	private final boolean[] matrix;

	private final Queue<Point> queue = new ArrayDeque<Point>();

	public TileAreaBuilder( ColorSelector colorSelector, Tile tile, ImageAccess img ) {
		this.colorSelector = colorSelector;
		this.tile = tile;
		this.img = img;
		
		this.matrix = new boolean[img.getWidth() * img.getHeight()];
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

	public void process() {
		while ( !queue.isEmpty() ) {
			Point p = queue.poll();

			if ( p.x > 0 )
				enqueue( new Point( p.x - 1, p.y ) );
			if ( p.x < img.getWidth() - 1 )
				enqueue( new Point( p.x + 1, p.y ) );
			if ( p.y > 0 )
				enqueue( new Point( p.x, p.y - 1 ) );
			if ( p.y < img.getHeight() - 1 )
				enqueue( new Point( p.x, p.y + 1 ) );
		}
	}

	public TileArea result() {
		return new TileArea( tile, matrix );
	}

}
