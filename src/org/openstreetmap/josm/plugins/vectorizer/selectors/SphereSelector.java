package org.openstreetmap.josm.plugins.vectorizer.selectors;

import java.awt.image.BufferedImage;

public class SphereSelector implements ColorSelector {

	private final int r, g, b;
	private final double sd;

	public SphereSelector( int center, double sd ) {
		this.r = (center >> 16) & 0xFF;
		this.g = (center >> 8) & 0xFF;
		this.b = center & 0xFF;
		this.sd = sd;
	}

	public SphereSelector( int r, int g, int b, double sd ) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.sd = sd;
	}

	public boolean test( int color ) {
		double d = 0;
		d += sqr( r - ((color >> 16) & 0xFF) );
		d += sqr( g - ((color >> 8) & 0xFF) );
		d += sqr( b - (color & 0xFF) );

		return Math.sqrt(d) <= sd;
	}

	public ColorSelector scale( double scale ) {
		return new SphereSelector( r, g, b, sd * scale );
	}

	private static double sqr( double x ) {
		return x*x;
	}

	public static SphereSelector average( BufferedImage img, int sx, int sy, int r, int sd ) {
		int rsum = 0, gsum = 0, bsum = 0;
		int count = 0;

		for ( int x = Math.max( sx - r, 0 ), xe = Math.min( sx + r + 1, img.getWidth() ); x < xe; ++x )
			for ( int y = Math.max( sy - r, 0 ), ye = Math.min( sy + r + 1, img.getWidth() ); y < ye; ++y ) {
				int c = img.getRGB( x, y );

				count++;
				rsum += (c >> 16) & 0xFF;
				gsum += (c >> 8) & 0xFF;
				bsum += (c >> 0) & 0xFF;
			}

		rsum /= count;
		gsum /= count;
		bsum /= count;

		return new SphereSelector( rsum, gsum, bsum, sd );
	}

}