package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Projection;

public class AreaNormales extends HashMap<TilePoint, Direction> {

	private final Area area;

	public AreaNormales( Area area ) {
		this.area = area;
	}

	private static final long serialVersionUID = 1L;

	public Map.Entry<TilePoint, Direction> popFirst() {
		Iterator<Entry<TilePoint, Direction>> it = entrySet().iterator();
		Map.Entry<TilePoint, Direction> e = it.next();
		it.remove();
		return e;
	}

	public Map.Entry<TilePoint, Direction> popFirst( TilePoint[] keys ) {
		for ( TilePoint key : keys ) {
			Direction val = remove( key );

			if ( val != null )
				return new AbstractMap.SimpleImmutableEntry<TilePoint, Direction>( key, val );
		}

		return null;
	}

	public Entry<TilePoint, Direction> popNext( Entry<TilePoint, Direction> e ) {
		return popFirst( getNextCandidates( e ) );
	}

	public List<Way> buildWays( double dx, double dy ) {
		double d = 10;

		List<Way> ways = new ArrayList<Way>();
		List<Node> nodes = new ArrayList<Node>(); // Nodes array is reused between iterations

		while ( !isEmpty() ) {
			nodes.clear();

			Map.Entry<TilePoint, Direction> e = popFirst();

			while ( e != null ) {
				nodes.add( buildNode( e.getKey(), dx, dy ) );
				e = popNext( e );
			}

			nodes.add( nodes.get( 0 ) );

			List<Node> newNodes = DouglasPeucker.simplify( nodes, d );

			if ( newNodes.size() > 4 ) {
				Way way = new Way();
				way.setNodes( newNodes );
				ways.add( way );
			}
		}

		return ways;
	}

	private Node buildNode( TilePoint p, double dx, double dy ) {
		Projection proj = Main.getProjection();
		LatLon ll = proj.eastNorth2latlon( proj.latlon2eastNorth( area.getLatLon( p, 2 ) ).add( dx, dy ) );

		return new Node( ll );
	}

	private TilePoint[] getNextCandidates( Map.Entry<TilePoint, Direction> e ) {
		TilePoint p = e.getKey();
		Point tp = new Point( p.tileX, p.tileY );

		switch ( e.getValue() ) {
		case LEFT:
			return new TilePoint[] { area.getTilePoint( tp, p.offsetX - 1, p.offsetY - 1, 2 ), area.getTilePoint( tp, p.offsetX, p.offsetY - 2, 2 ),
					area.getTilePoint( tp, p.offsetX + 1, p.offsetY - 1, 2 ) };
		case RIGHT:
			return new TilePoint[] { area.getTilePoint( tp, p.offsetX + 1, p.offsetY + 1, 2 ), area.getTilePoint( tp, p.offsetX, p.offsetY + 2, 2 ),
					area.getTilePoint( tp, p.offsetX - 1, p.offsetY + 1, 2 ) };
		case UP:
			return new TilePoint[] { area.getTilePoint( tp, p.offsetX + 1, p.offsetY - 1, 2 ), area.getTilePoint( tp, p.offsetX + 2, p.offsetY, 2 ),
					area.getTilePoint( tp, p.offsetX + 1, p.offsetY + 1, 2 ) };
		case DOWN:
			return new TilePoint[] { area.getTilePoint( tp, p.offsetX - 1, p.offsetY - 1, 2 ), area.getTilePoint( tp, p.offsetX - 2, p.offsetY, 2 ),
					area.getTilePoint( tp, p.offsetX - 1, p.offsetY + 1, 2 ) };
		}

		throw new Error();
	}

}
