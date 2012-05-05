package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.ScanexTileSource;
import org.openstreetmap.josm.data.coor.LatLon;

public class Area {

	private final TileSource source;

	private final int zoom;

	private final Map<Point, TileArea> tiles;

	public Area( TileSource source, int zoom, Map<Point, TileArea> tiles ) {
		this.source = source;
		this.zoom = zoom;
		this.tiles = tiles;
	}

	public Collection<TileArea> getTileAreas() {
		return tiles.values();
	}

	public TileArea getTileArea( Point tileCoord ) {
		return tiles.get( tileCoord );
	}

	public void paint() {
		for ( TileArea tile : tiles.values() )
			tile.paint();
	}

	public TilePoint getTilePoint( Point tilePoint, int x, int y, int m ) {
		TileArea tileArea = tiles.get( tilePoint );

		if ( tileArea == null )
			return new TilePoint( tilePoint.x, tilePoint.y, x, y );

		return tileArea.getTilePoint( x, y, m, this );
	}

	public boolean contains( Point tilePoint, int x, int y ) {
		TileArea tileArea = tiles.get( tilePoint );

		if ( tileArea == null )
			return false;

		return tileArea.contains( x, y, this );
	}

	private static double RADIUS_E = 6378137; // radius of Earth at equator, m
	private static double EQUATOR = 40075016.68557849; // equator length, m
	private static double E = 0.0818191908426; // eccentricity of Earth's ellipsoid

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

	public LatLon getLatLon( TilePoint p, double m ) {
		double tx = getTx( p, m );
		double ty = getTy( p, m );

		if ( source instanceof ScanexTileSource ) {
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

		} else if ( source instanceof AbstractTMSTileSource ) {
			double lat = Math.atan( Math.sinh( Math.PI - (Math.PI * ty / Math.pow( 2.0, zoom - 1 )) ) ) * 180 / Math.PI;
			double lon = tx * 45.0 / Math.pow( 2.0, zoom - 3 ) - 180.0;

			return new LatLon( lat, lon );
		}

		throw new Error( "Unknown tile source type: " + source.getClass() );
	}

	protected double getTy( TilePoint p, double m ) {
		if ( p.offsetY == 0 )
			return p.tileY;

		TileArea ta = tiles.get( new Point( p.tileX, p.tileY ) );

		if ( ta == null && p.offsetX < 2 )
			ta = tiles.get( new Point( p.tileX - 1, p.tileY ) );

		return ta.tile.getYtile() + p.offsetY / m / ta.tile.getImage().getHeight();
	}

	protected double getTx( TilePoint p, double m ) {
		if ( p.offsetX == 0 )
			return p.tileX;

		TileArea ta = tiles.get( new Point( p.tileX, p.tileY ) );

		if ( ta == null && p.offsetY < 2 )
			ta = tiles.get( new Point( p.tileX, p.tileY - 1 ) );

		return ta.tile.getXtile() + p.offsetX / m / ta.tile.getImage().getWidth();
	}

}
