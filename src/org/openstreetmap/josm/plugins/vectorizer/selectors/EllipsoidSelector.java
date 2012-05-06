package org.openstreetmap.josm.plugins.vectorizer.selectors;

import java.awt.image.BufferedImage;

public class EllipsoidSelector implements ColorSelector {

	private final double cr, cg, cb;
	private final double sr, sg, sb;

	public EllipsoidSelector( double cr, double cg, double cb, double sr, double sg, double sb ) {
		this.cr = cr;
		this.cg = cg;
		this.cb = cb;
		this.sr = sr;
		this.sg = sg;
		this.sb = sb;
	}

	public boolean test( int color ) {
		double d = 0;
		d += sqr( (cr - ((color >> 16) & 0xFF)) / sr );
		d += sqr( (cg - ((color >> 8) & 0xFF)) / sg );
		d += sqr( (cb - (color & 0xFF)) / sb );

		return Math.sqrt( d ) <= 1;
	}

	public EllipsoidSelector scale( double scale ) {
		return new EllipsoidSelector( cr, cg, cb, sr * scale, sg * scale, sb * scale );
	}

	public EllipsoidSelector expand( double radius ) {
		return new EllipsoidSelector( cr, cg, cb, sr + radius, sg + radius, sb + radius );
	}

	private static double sqr( double x ) {
		return x * x;
	}

	public static EllipsoidSelector average( BufferedImage img, int sx, int sy, int r) {
		double cr = 0, cg = 0, cb = 0;
		int count = 0;

		for ( int x = Math.max( sx - r, 0 ), xe = Math.min( sx + r + 1, img.getWidth() ); x < xe; ++x )
			for ( int y = Math.max( sy - r, 0 ), ye = Math.min( sy + r + 1, img.getWidth() ); y < ye; ++y ) {
				int c = img.getRGB( x, y );

				count++;
				cr += (c >> 16) & 0xFF;
				cg += (c >> 8) & 0xFF;
				cb += (c >> 0) & 0xFF;
			}

		cr /= count;
		cg /= count;
		cb /= count;

		double sr = 0, sg = 0, sb = 0;

		for ( int x = Math.max( sx - r, 0 ), xe = Math.min( sx + r + 1, img.getWidth() ); x < xe; ++x )
			for ( int y = Math.max( sy - r, 0 ), ye = Math.min( sy + r + 1, img.getWidth() ); y < ye; ++y ) {
				int c = img.getRGB( x, y );

				sr += sqr( ((c >> 16) & 0xFF) - cr );
				sg += sqr( ((c >> 8) & 0xFF) - cg );
				sb += sqr( ((c >> 0) & 0xFF) - cb );
			}

		sr = 3 * Math.sqrt( sr / count );
		sg = 3 * Math.sqrt( sg / count );
		sb = 3 * Math.sqrt( sb / count );

		return new EllipsoidSelector( cr, cg, cb, sr, sg, sb );
	}

}
