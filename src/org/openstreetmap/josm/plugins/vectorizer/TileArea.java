package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.image.BufferedImage;
import java.util.Random;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.ScanexTileSource;
import org.openstreetmap.josm.data.coor.LatLon;

public class TileArea {

	private static double RADIUS_E = 6378137; // radius of Earth at equator, m
	private static double EQUATOR = 40075016.68557849; // equator length, m
	private static double E = 0.0818191908426; // eccentricity of Earth's ellipsoid

	public final Tile tile;

	public final boolean[] matrix;

	public int getMatrixWidth() {
		return tile.getImage().getWidth();
	}

	public int getMatrixHeight() {
		return tile.getImage().getWidth();
	}

	public TileArea( Tile tile, boolean[] matrix ) {
		this.tile = tile;
		this.matrix = matrix;
	}

	public void paint() {
		BufferedImage img = tile.getImage();

		for ( int x = 0, ex = img.getWidth(); x < ex; ++x )
			for ( int y = 0, ey = img.getHeight(); y < ey; ++y )
				if ( matrix[x + y * ex] )
					img.setRGB( x, y, 0x0000ff );
	}

	private double cached_lat = 54;

	private double NextTerm( double lat, double y, int zoom ) {
		double sinl = Math.sin( lat );
		double cosl = Math.cos( lat );
		double ec, f, df;

		zoom = (int) Math.pow( 2.0, zoom - 1 );
		ec = Math.exp( (1 - y / zoom) * Math.PI );

		f = (Math.tan( Math.PI / 4 + lat / 2 ) - ec * Math.pow( Math.tan( Math.PI / 4 + Math.asin( E * sinl ) / 2 ), E ));
		df = 1 / (1 - sinl) - ec * E * cosl / ((1 - E * sinl) * (Math.sqrt( 1 - E * E * sinl * sinl )));

		return (f / df);
	}

	public LatLon getLatLon( double mx, double my ) {
		TileSource src = tile.getSource();
		int zoom = tile.getZoom();
		double tx = tile.getXtile() + mx / getMatrixWidth();
		double ty = tile.getYtile() + my / getMatrixHeight();

		if ( src instanceof ScanexTileSource ) {
			Random r = new Random();
			double lat0, lat;

			lat = cached_lat;
			do {
				lat0 = lat;
				lat = lat - Math.toDegrees( NextTerm( Math.toRadians( lat ), ty, zoom ) );
				if ( lat > OsmMercator.MAX_LAT || lat < OsmMercator.MIN_LAT ) {
					lat = OsmMercator.MIN_LAT + (double) r.nextInt( (int) (OsmMercator.MAX_LAT - OsmMercator.MIN_LAT) );
				}
			} while ( (Math.abs( lat0 - lat ) > 0.000001) );

			cached_lat = lat;

			double lon = (tx / Math.pow( 2.0, zoom - 1 ) - 1) * (90 * EQUATOR) / RADIUS_E / Math.PI;

			return new LatLon( lat, lon );

		} else if ( src instanceof AbstractTMSTileSource ) {
			double lat = Math.atan( Math.sinh( Math.PI - (Math.PI * ty / Math.pow( 2.0, zoom - 1 )) ) ) * 180 / Math.PI;
			double lon = tx * 45.0 / Math.pow( 2.0, zoom - 3 ) - 180.0;

			return new LatLon( lat, lon );
		}

		throw new Error( "Unknown tile source type: " + src.getClass() );
	}

}
