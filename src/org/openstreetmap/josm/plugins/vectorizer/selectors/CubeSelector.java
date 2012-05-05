package org.openstreetmap.josm.plugins.vectorizer.selectors;

import java.awt.image.BufferedImage;

public class CubeSelector implements ColorSelector {

	private final int r, g, b;
	private final int sd;

	public CubeSelector( int center, int sd ) {
		this.r = (center >> 16) & 0xFF;
		this.g = (center >> 8) & 0xFF;
		this.b = center & 0xFF;
		this.sd = sd;
	}

	public CubeSelector( int r, int g, int b, int sd ) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.sd = sd;
	}

	public boolean test( int color ) {
		int d = 0;
		d += Math.abs( r - ((color >> 16) & 0xFF) );
		d += Math.abs( g - ((color >> 8) & 0xFF) );
		d += Math.abs( b - (color & 0xFF) );

		return d <= sd;
	}

	public ColorSelector scale( double scale ) {
		return new CubeSelector( r, g, b, (int) Math.round( sd * scale ) );
	}

	public static CubeSelector average( BufferedImage img, int sx, int sy, int r, int sd ) {
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

		return new CubeSelector( rsum, gsum, bsum, sd );
	}

}
