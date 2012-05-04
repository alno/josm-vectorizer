package org.openstreetmap.josm.plugins.vectorizer;

import java.util.Arrays;

public class MedianImageAccess implements ImageAccess {

	private final ImageAccess next;
	private final int d;

	public MedianImageAccess( ImageAccess next, int dist ) {
		this.next = next;
		this.d = dist;
	}

	public int getWidth() {
		return next.getWidth();
	}

	public int getHeight() {
		return next.getHeight();
	}

	public int getRGB( int cx, int cy ) {

		int[] r = new int[(2 * d + 1) * (2 * d + 1)];
		int[] g = new int[(2 * d + 1) * (2 * d + 1)];
		int[] b = new int[(2 * d + 1) * (2 * d + 1)];
		int c = 0;

		Arrays.fill( r, 1000 );
		Arrays.fill( g, 1000 );
		Arrays.fill( b, 1000 );

		for ( int x = Math.max( cx - d, 0 ), ex = Math.min( cx + d + 1, getWidth() ); x < ex; ++x )
			for ( int y = Math.max( cy - d, 0 ), ey = Math.min( cy + d + 1, getHeight() ); y < ey; ++y ) {
				int col = next.getRGB( x, y );

				r[c] = (col >> 16) & 0xFF;
				g[c] = (col >> 8) & 0xFF;
				b[c] = (col >> 0) & 0xFF;
				c++;
			}

		Arrays.sort( r );
		Arrays.sort( g );
		Arrays.sort( b );

		return (r[c / 2] << 16) | (g[c / 2] << 8) | b[c / 2];
	}
}
