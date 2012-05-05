package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class AreaVectorizer {

	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;

	public List<Way> vectorize( Area area ) {
		Map<TilePoint, Integer> normales = new HashMap<TilePoint, Integer>();

		for ( TileArea tileArea : area.getTileAreas() )
			for ( int x = 0, xe = tileArea.getMatrixWidth(); x < xe; ++x )
				for ( int y = 0, ye = tileArea.getMatrixHeight(); y < ye; ++y )
					if ( tileArea.matrix[x + y * xe] ) {
						if ( !tileArea.contains( x - 1, y, area ) )
							normales.put( tileArea.getTilePoint( 2 * x + 0, 2 * y + 1, 2, area ), LEFT );
						if ( !tileArea.contains( x + 1, y, area ) )
							normales.put( tileArea.getTilePoint( 2 * x + 2, 2 * y + 1, 2, area ), RIGHT );

						if ( !tileArea.contains( x, y - 1, area ) )
							normales.put( tileArea.getTilePoint( 2 * x + 1, 2 * y + 0, 2, area ), UP );
						if ( !tileArea.contains( x, y + 1, area ) )
							normales.put( tileArea.getTilePoint( 2 * x + 1, 2 * y + 2, 2, area ), DOWN );
					}

		ArrayList<Way> ways = new ArrayList<Way>();

		double d = 10;

		while ( !normales.isEmpty() ) {
			ArrayList<Node> nodes = new ArrayList<Node>();

			Map.Entry<TilePoint, Integer> e = first( normales );

			while ( e != null ) {
				nodes.add( new Node( area.getLatLon( e.getKey(), 2 ) ) );
				e = first( normales, next( area, e ) );
			}

			nodes.add( nodes.get( 0 ) );

			ArrayList<Node> newNodes = new ArrayList<Node>();

			buildSimplifiedNodeList( nodes, 0, nodes.size() - 1, d, newNodes );

			if ( newNodes.size() > 3 ) {
				Way way = new Way();
				way.setNodes( newNodes );
				ways.add( way );
			}
		}

		return ways;
	}

	private TilePoint[] next( Area a, Map.Entry<TilePoint, Integer> e ) {
		TilePoint p = e.getKey();
		Point tp = new Point( p.tileX, p.tileY );

		switch ( e.getValue() ) {
		case LEFT:
			return new TilePoint[] { a.getTilePoint( tp, p.offsetX - 1, p.offsetY - 1, 2 ), a.getTilePoint( tp, p.offsetX, p.offsetY - 2, 2 ),
					a.getTilePoint( tp, p.offsetX + 1, p.offsetY - 1, 2 ) };
		case RIGHT:
			return new TilePoint[] { a.getTilePoint( tp, p.offsetX + 1, p.offsetY + 1, 2 ), a.getTilePoint( tp, p.offsetX, p.offsetY + 2, 2 ),
					a.getTilePoint( tp, p.offsetX - 1, p.offsetY + 1, 2 ) };
		case UP:
			return new TilePoint[] { a.getTilePoint( tp, p.offsetX + 1, p.offsetY - 1, 2 ), a.getTilePoint( tp, p.offsetX + 2, p.offsetY, 2 ),
					a.getTilePoint( tp, p.offsetX + 1, p.offsetY + 1, 2 ) };
		case DOWN:
			return new TilePoint[] { a.getTilePoint( tp, p.offsetX - 1, p.offsetY - 1, 2 ), a.getTilePoint( tp, p.offsetX - 2, p.offsetY, 2 ),
					a.getTilePoint( tp, p.offsetX - 1, p.offsetY + 1, 2 ) };
		}

		throw new Error();
	}

	private Map.Entry<TilePoint, Integer> first( Map<TilePoint, Integer> normales ) {
		Iterator<Entry<TilePoint, Integer>> it = normales.entrySet().iterator();
		Map.Entry<TilePoint, Integer> e = it.next();
		it.remove();
		return e;
	}

	private Map.Entry<TilePoint, Integer> first( Map<TilePoint, Integer> normales, TilePoint[] keys ) {
		for ( TilePoint key : keys ) {
			Integer val = normales.remove( key );

			if ( val != null )
				return new AbstractMap.SimpleImmutableEntry<TilePoint, Integer>( key, val );
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
