package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.vectorizer.imageaccess.DirectImageAccess;
import org.openstreetmap.josm.plugins.vectorizer.imageaccess.ImageAccess;
import org.openstreetmap.josm.plugins.vectorizer.imageaccess.MedianImageAccess;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;
import org.openstreetmap.josm.plugins.vectorizer.selectors.EllipsoidSelector;

public class VectorizeTask extends PleaseWaitRunnable {

	private final ImageryLayer layer; // Source layer for vectorization
	private final Point p; // Starting point of vectorization

	private volatile boolean cancelled = false; // Is task cancelled?

	public VectorizeTask( String title, ImageryLayer layer, Point p ) {
		super( title );
		this.layer = layer;
		this.p = p;
	}

	public ColorSelector createColorSelector( BufferedImage img, int sx, int sy ) {
		return EllipsoidSelector.average( img, sx, sy, 2, 0 ).expand( 10 );
	}

	public ImageAccess createImageAccess( BufferedImage img ) {
		return new MedianImageAccess( new DirectImageAccess( img ), 1 );
	}

	@Override
	protected void realRun() {
		try {
			final List<Way> ways = vectorize( layer, p );

			if ( ways.isEmpty() )
				return;

			final SequenceCommand command = new SequenceCommand( tr( "Vectorizing area" ), buildCommands( ways ) );

			progressMonitor.subTask( tr( "Appending commands" ) );
			SwingUtilities.invokeLater( new Runnable() {

				public void run() {
					Main.main.undoRedo.add( command );
					Main.main.getCurrentDataSet().setSelected( ways );
				}

			} );
		} catch ( CancelledException e ) {
			System.out.println( "Vectorizing task cancelled" );
		}
	}

	@Override
	protected void finish() {
	}

	@Override
	protected void cancel() {
		cancelled = true;
	}

	/**
	 * Build sequence of commands for vectorized ways
	 * 
	 * @throws CancelledException
	 */
	protected List<Command> buildCommands( List<Way> ways ) throws CancelledException {
		List<Command> commands = new ArrayList<Command>();
		Set<Node> markedNodes = new HashSet<Node>();

		progressMonitor.subTask( tr( "Building commands" ) );
		progressMonitor.setTicksCount( ways.size() );
		progressMonitor.setTicks( 0 );

		int progress = 0;
		for ( Way w : ways ) {
			commands.add( new SequenceCommand( tr( "Building way" ), buildWayCommands( markedNodes, w ) ) );
			progressMonitor.setTicks( ++progress );
		}

		return commands;
	}

	/**
	 * Build sequence of commands for single way
	 * 
	 * @throws CancelledException
	 */
	protected List<Command> buildWayCommands( Set<Node> markedNodes, Way way ) throws CancelledException {
		List<Command> wayCommands = new ArrayList<Command>();

		for ( Node n : way.getNodes() ) {
			if ( cancelled )
				throw new CancelledException();

			if ( !markedNodes.contains( n ) ) {
				wayCommands.add( new AddCommand( n ) );
				markedNodes.add( n );
			}
		}

		wayCommands.add( new AddCommand( way ) );

		return wayCommands;
	}

	protected List<Way> vectorize( ImageryLayer layer, Point pos ) {
		if ( layer instanceof TMSLayer )
			return vectorizeTMS( (TMSLayer) layer, pos );

		return Collections.emptyList();
	}

	private List<Way> vectorizeTMS( TMSLayer layer, Point pos ) {
		progressMonitor.subTask( tr( "Determining area" ) );
		Area area = select( layer, Main.map.mapView.getLatLon( pos.x - layer.getDx(), pos.y - layer.getDy() ) );

		progressMonitor.subTask( tr( "Building normales" ) );
		AreaNormales normales = area.buildNormales();

		progressMonitor.subTask( tr( "Building ways" ) );
		return normales.buildWays();
	}

	private Area select( TMSLayer layer, LatLon ll ) {
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