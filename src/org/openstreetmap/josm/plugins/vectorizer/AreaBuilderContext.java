package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.vectorizer.imageaccess.ImageAccess;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;

public abstract class AreaBuilderContext {

	public abstract ColorSelector createColorSelector( BufferedImage img, int sx, int sy );

	public abstract ImageAccess createImageAccess( BufferedImage img );

	public Area select( TMSLayer layer, LatLon ll ) {
		TileSource source = TMSLayer.getTileSource( layer.getInfo() );
		TileCache cache = layer.getTileCache();
		int zoom = layer.currentZoomLevel;

		double tileX = source.lonToTileX( ll.lon(), zoom );
		double tileY = source.latToTileY( ll.lat(), zoom );
		Point tileCoord = new Point( (int) tileX, (int) tileY );

		Tile tile = cache.getTile( source, (int) tileX, (int) tileY, zoom );
		BufferedImage img = tile.getImage();

		int ofsX = (int) Math.round( (tileX - (int) tileX) * img.getWidth() );
		int ofsY = (int) Math.round( (tileY - (int) tileY) * img.getHeight() );
		Point pointCoord = new Point( ofsX, ofsY );

		ColorSelector colorSelector = createColorSelector( img, ofsX, ofsY );
		AreaBuilder b = new AreaBuilder( this, colorSelector, source, cache, zoom );

		b.enqueue( tileCoord, pointCoord );
		b.process();

		return b.result();
	}
}
