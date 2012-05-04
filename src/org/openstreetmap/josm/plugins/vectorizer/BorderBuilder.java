package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class BorderBuilder {

	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;

	public List<Way> build( TileArea area ) {
		Map<Point, Integer> normales = new HashMap<Point, Integer>();

		for ( int x = 0, xe = area.getMatrixWidth(); x < xe; ++x )
			for ( int y = 0, ye = area.getMatrixHeight(); y < ye; ++y )
				if ( area.matrix[x + y * xe] ) {
					if ( x <= 0 || !area.matrix[x - 1 + y * xe] )
						normales.put( new Point( 2 * x - 1, 2 * y ), LEFT );
					if ( x >= xe - 1 || !area.matrix[x + 1 + y * xe] )
						normales.put( new Point( 2 * x + 1, 2 * y ), RIGHT );

					if ( y <= 0 || !area.matrix[x + (y - 1) * xe] )
						normales.put( new Point( 2 * x, 2 * y - 1 ), UP );
					if ( y >= ye - 1 || !area.matrix[x + (y + 1) * xe] )
						normales.put( new Point( 2 * x, 2 * y + 1 ), DOWN );
				}

		ArrayList<Way> ways = new ArrayList<Way>();

		double d = 10;

		while ( !normales.isEmpty() ) {
			ArrayList<Node> nodes = new ArrayList<Node>();

			Map.Entry<Point, Integer> e = first( normales );

			while ( e != null ) {
				nodes.add( buildNode( area, e.getKey() ) );
				e = first( normales, next( e ) );
			}

			ArrayList<Node> newNodes = new ArrayList<Node>();

			buildSimplifiedNodeList( nodes, 0, nodes.size() - 1, d, newNodes );

			if ( newNodes.size() > 3 ) {
				newNodes.add( newNodes.get( 0 ) );

				Way way = new Way();
				way.setNodes( newNodes );

				ways.add( way );
			}
		}

		return ways;
	}

	private static double RADIUS_E = 6378137; /* radius of Earth at equator, m */
	private static double EQUATOR = 40075016.68557849; /* equator length, m */
	private static double E = 0.0818191908426; /*
												 * eccentricity of Earth's
												 * ellipsoid
												 */

	private Node buildNode( TileArea a, Point p ) {
		return new Node( a.getLatLon( (p.x + 1) * 0.5, (p.y + 1) * 0.5 ) );
	}

	private Point[] next( Map.Entry<Point, Integer> e ) {
		Point p = e.getKey();

		switch ( e.getValue() ) {
		case LEFT:
			return new Point[] { new Point( p.x - 1, p.y - 1 ), new Point( p.x, p.y - 2 ), new Point( p.x + 1, p.y - 1 ) };
		case RIGHT:
			return new Point[] { new Point( p.x + 1, p.y + 1 ), new Point( p.x, p.y + 2 ), new Point( p.x - 1, p.y + 1 ) };
		case UP:
			return new Point[] { new Point( p.x + 1, p.y - 1 ), new Point( p.x + 2, p.y ), new Point( p.x + 1, p.y + 1 ) };
		case DOWN:
			return new Point[] { new Point( p.x - 1, p.y - 1 ), new Point( p.x - 2, p.y ), new Point( p.x - 1, p.y + 1 ) };
		}

		throw new Error();
	}

	private Map.Entry<Point, Integer> first( Map<Point, Integer> normales ) {
		Iterator<Entry<Point, Integer>> it = normales.entrySet().iterator();
		Map.Entry<Point, Integer> e = it.next();
		it.remove();
		return e;
	}

	private Map.Entry<Point, Integer> first( Map<Point, Integer> normales, Point[] keys ) {
		for ( Point key : keys ) {
			Integer val = normales.remove( key );

			if ( val != null )
				return new AbstractMap.SimpleImmutableEntry<Point, Integer>( key, val );
		}

		return null;
	}

	protected void buildSimplifiedNodeList( List<Node> wnew, int from, int to, double threshold, List<Node> simplifiedNodes ) {

		Node fromN = wnew.get( from );
		Node toN = wnew.get( to );

		// Get max xte
		int imax = -1;
		double xtemax = 0;
		for ( int i = from + 1; i < to; i++ ) {
			Node n = wnew.get( i );
			double xte = Math.abs( EARTH_RAD
					* xtd( fromN.getCoor().lat() * Math.PI / 180, fromN.getCoor().lon() * Math.PI / 180, toN.getCoor().lat() * Math.PI / 180, toN.getCoor().lon() * Math.PI / 180,
							n.getCoor().lat() * Math.PI / 180, n.getCoor().lon() * Math.PI / 180 ) );
			if ( xte > xtemax ) {
				xtemax = xte;
				imax = i;
			}
		}

		if ( imax != -1 && xtemax >= threshold ) {
			// Segment cannot be simplified - try shorter segments
			buildSimplifiedNodeList( wnew, from, imax, threshold, simplifiedNodes );
			//simplifiedNodes.add(wnew.get(imax));
			buildSimplifiedNodeList( wnew, imax, to, threshold, simplifiedNodes );
		} else {
			// Simplify segment
			if ( simplifiedNodes.isEmpty() || simplifiedNodes.get( simplifiedNodes.size() - 1 ) != fromN ) {
				simplifiedNodes.add( fromN );
			}
			if ( fromN != toN ) {
				simplifiedNodes.add( toN );
			}
		}
	}

	public static final double EARTH_RAD = 6378137.0;

	/*
	 * From Aviaton Formulary v1.3 http://williams.best.vwh.net/avform.htm
	 */
	public static double dist( double lat1, double lon1, double lat2, double lon2 ) {
		return 2 * Math.asin( Math.sqrt( Math.pow( Math.sin( (lat1 - lat2) / 2 ), 2 ) + Math.cos( lat1 ) * Math.cos( lat2 ) * Math.pow( Math.sin( (lon1 - lon2) / 2 ), 2 ) ) );
	}

	public static double course( double lat1, double lon1, double lat2, double lon2 ) {
		return Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ), Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) )
				% (2 * Math.PI);
	}

	public static double xtd( double lat1, double lon1, double lat2, double lon2, double lat3, double lon3 ) {
		double dist_AD = dist( lat1, lon1, lat3, lon3 );
		double crs_AD = course( lat1, lon1, lat3, lon3 );
		double crs_AB = course( lat1, lon1, lat2, lon2 );
		return Math.asin( Math.sin( dist_AD ) * Math.sin( crs_AD - crs_AB ) );
	}

}
