package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;

public abstract class AreaSelector {

	public abstract ColorSelector createColorSelector( BufferedImage img, int sx, int sy );

	public TileArea select( TMSLayer layer, LatLon ll ) {
		TileSource source = TMSLayer.getTileSource( layer.getInfo() );
		TileCache cache = layer.getTileCache();
		int zoom = layer.currentZoomLevel;

		double tileX = source.lonToTileX( ll.lon(), zoom );
		double tileY = source.latToTileY( ll.lat(), zoom );

		Tile tile = cache.getTile( source, (int) tileX, (int) tileY, zoom );
		BufferedImage img = tile.getImage();

		int ofsX = (int) Math.round( (tileX - (int) tileX) * img.getWidth() );
		int ofsY = (int) Math.round( (tileY - (int) tileY) * img.getHeight() );

		ColorSelector colorSelector = createColorSelector( img, ofsX, ofsY );
		TileAreaBuilder b = new TileAreaBuilder( colorSelector, tile, new MedianImageAccess(new DirectImageAccess(tile.getImage()), 1) );

		b.enqueue( new Point( ofsX, ofsY ) );
		b.process();

		return b.result();
	}
}
