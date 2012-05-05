package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;

public class AreaBuilder {

	public final AreaBuilderContext ctx;
	public final ColorSelector colorSelector;

	private final TileSource source;
	private final TileCache cache;
	private final int zoom;

	private final Map<Point, TileAreaBuilder> tiles = new HashMap<Point, TileAreaBuilder>();

	public AreaBuilder( AreaBuilderContext ctx, ColorSelector colorSelector, TileSource source, TileCache cache, int zoom ) {
		this.ctx = ctx;
		this.colorSelector = colorSelector;
		this.source = source;
		this.cache = cache;
		this.zoom = zoom;
	}

	public void enqueue( Point tile, Point ofs ) {
		TileAreaBuilder tileBuilder = getTileBuilder( tile );

		if ( tileBuilder != null )
			tileBuilder.enqueue( ofs );
	}

	public void process() {
		HashSet<TileAreaBuilder> tb = new HashSet<TileAreaBuilder>();

		while ( queued() ) {
			tb.addAll( tiles.values() );

			for ( TileAreaBuilder b : tb )
				b.process();
		}
	}

	public boolean queued() {
		for ( TileAreaBuilder b : tiles.values() )
			if ( b.queued() )
				return true;

		return false;
	}

	protected TileAreaBuilder getTileBuilder( Point tileCoord ) {
		TileAreaBuilder res = tiles.get( tileCoord );

		if ( res != null )
			return res;

		Tile tile = cache.getTile( source, (int) tileCoord.x, (int) tileCoord.y, zoom );

		if ( tile == null )
			return null;

		res = new TileAreaBuilder( this, tile );
		tiles.put( tileCoord, res );
		return res;
	}

	public Area result() {
		Map<Point, TileArea> tileAreas = new HashMap<Point, TileArea>();

		for ( Map.Entry<Point, TileAreaBuilder> e : tiles.entrySet() )
			tileAreas.put( e.getKey(), e.getValue().result() );

		return new Area( source, zoom, tileAreas );
	}

}
