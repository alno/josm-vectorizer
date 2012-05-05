package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
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

	private static final AreaBuilderContext areaBuilderContext = new AreaBuilderContext() {

		@Override
		public ColorSelector createColorSelector( BufferedImage img, int sx, int sy ) {
			return EllipsoidSelector.average( img, sx, sy, 2, 0 ).expand( 10 );
		}

		@Override
		public ImageAccess createImageAccess( BufferedImage img ) {
			return new MedianImageAccess( new DirectImageAccess( img ), 1 );
		}

	};

	private final ImageryLayer layer; // Source layer for vectorization
	private final Point p; // Starting point of vectorization

	public VectorizeTask( String title, ImageryLayer layer, Point p ) {
		super( title );
		this.layer = layer;
		this.p = p;
	}

	@Override
	protected void realRun() {
		final List<Way> ways = vectorize( layer, p );

		if ( ways.isEmpty() )
			return;

		progressMonitor.subTask( tr( "Preparing commands" ) );
		Main.main.undoRedo.add( new SequenceCommand( tr( "Vectorizing area" ), buildCommands( ways ) ) );

		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				Main.main.getCurrentDataSet().setSelected( ways );
			}

		} );
	}

	@Override
	protected void finish() {
	}

	@Override
	protected void cancel() {
		// TODO
	}

	/**
	 * Build sequence of commands for vectorized ways
	 */
	protected List<Command> buildCommands( List<Way> ways ) {
		List<Command> commands = new ArrayList<Command>();
		Set<Node> markedNodes = new HashSet<Node>();

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
	 */
	protected List<Command> buildWayCommands( Set<Node> markedNodes, Way way ) {
		List<Command> wayCommands = new ArrayList<Command>();

		for ( Node n : way.getNodes() )
			if ( !markedNodes.contains( n ) ) {
				wayCommands.add( new AddCommand( n ) );
				markedNodes.add( n );
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
		Area area = areaBuilderContext.select( layer, Main.map.mapView.getLatLon( pos.x - layer.getDx(), pos.y - layer.getDy() ) );

		progressMonitor.subTask( tr( "Vectorizing area" ) );
		return new AreaVectorizer().vectorize( area );
	}
}