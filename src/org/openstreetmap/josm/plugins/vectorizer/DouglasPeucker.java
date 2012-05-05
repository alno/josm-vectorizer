package org.openstreetmap.josm.plugins.vectorizer;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;

public class DouglasPeucker {

	public static List<Node> simplify( List<Node> nodes, double threshold ) {
		ArrayList<Node> out = new ArrayList<Node>();

		buildSimplifiedNodeList( nodes, 0, nodes.size() - 1, threshold, out );

		return out;
	}

	public static void buildSimplifiedNodeList( List<Node> wnew, int from, int to, double threshold, List<Node> simplifiedNodes ) {

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
