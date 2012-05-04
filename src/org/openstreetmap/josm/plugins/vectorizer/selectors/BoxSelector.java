package org.openstreetmap.josm.plugins.vectorizer.selectors;

import java.awt.image.BufferedImage;

public class BoxSelector implements ColorSelector {

	private final int minr, ming, minb;
	private final int maxr, maxg, maxb;

	public BoxSelector( int minr, int ming, int minb, int maxr, int maxg, int maxb ) {
		this.minr = minr;
		this.ming = ming;
		this.minb = minb;
		this.maxr = maxr;
		this.maxg = maxg;
		this.maxb = maxb;
	}

	public BoxSelector scale( double scale ) {
		double cr = (maxr + minr) / 2.0, dr = (maxr - cr) * scale;
		double cg = (maxg + ming) / 2.0, dg = (maxg - cg) * scale;
		double cb = (maxb + minb) / 2.0, db = (maxg - cb) * scale;

		return new BoxSelector( (int) Math.round( cr - dr ), (int) Math.round( cg - dg ), (int) Math.round( cb - db ), (int) Math.round( cr + dr ), (int) Math.round( cg + dg ),
				(int) Math.round( cb + db ) );
	}

	public boolean test( int color ) {
		int r = ((color >> 16) & 0xFF);
		int g = ((color >> 8) & 0xFF);
		int b = (color & 0xFF);

		return r >= minr && r <= maxr && g >= ming && g <= maxg && b >= minb && b <= maxb;
	}

	public static BoxSelector average( BufferedImage img, int sx, int sy, int d ) {

		int minr = 255, ming = 255, minb = 255;
		int maxr = 0, maxg = 0, maxb = 0;

		for ( int x = Math.max( sx - d, 0 ), xe = Math.min( sx + d + 1, img.getWidth() ); x < xe; ++x )
			for ( int y = Math.max( sy - d, 0 ), ye = Math.min( sy + d + 1, img.getWidth() ); y < ye; ++y ) {
				int c = img.getRGB( x, y );

				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c >> 0) & 0xFF;

				minr = Math.min( r, minr );
				ming = Math.min( g, ming );
				minb = Math.min( b, minb );
				maxr = Math.max( r, maxr );
				maxg = Math.max( g, maxg );
				maxb = Math.max( b, maxb );
			}

		return new BoxSelector( minr, ming, minb, maxr, maxg, maxb );
	}

}