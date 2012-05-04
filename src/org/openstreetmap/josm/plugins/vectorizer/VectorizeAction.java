package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.vectorizer.selectors.ColorSelector;
import org.openstreetmap.josm.plugins.vectorizer.selectors.EllipsoidSelector;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class VectorizeAction extends JosmAction {

	private static final long serialVersionUID = 1L;

	private static final Shortcut shortcut = Shortcut.registerShortcut( "tools:vectorize", tr( "Tool: {0}", tr( "Vectorizer" ) ), KeyEvent.VK_L, Shortcut.ALT_CTRL_SHIFT );

	private final MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mouseClicked( MouseEvent e ) {
			if ( active ) {
				Main.map.mapView.setCursor( oldCursor );
				Main.map.mapView.removeMouseListener( mouseListener );

				oldCursor = null;
				active = false;

				List<ImageryLayer> imageryLayers = Main.map.mapView.getLayersOfType( ImageryLayer.class );

				if ( imageryLayers.isEmpty() ) {
					JOptionPane.showMessageDialog( null, tr( "No imagery layers found!" ) );
					return;
				}

				List<Way> ways = vectorize( imageryLayers.get( 0 ), e.getPoint() );

				if ( ways.isEmpty() )
					return;

				ArrayList<Command> commands = new ArrayList<Command>();

				for ( Way w : ways ) {
					for ( Node n : w.getNodes().subList( 0, w.getNodesCount() - 1 ) )
						commands.add( new AddCommand( n ) );

					commands.add( new AddCommand( w ) );
				}

				Main.main.undoRedo.add( new SequenceCommand( tr( "Lakewalker trace" ), commands ) );
				Main.main.getCurrentDataSet().setSelected( ways );
			}
		}

	};

	private boolean active = false;
	private Cursor oldCursor = null;

	public VectorizeAction( String name ) {
		super( name, "lakewalker-sml", tr( "Vectorize feature." ), shortcut, true );
		
		setEnabled( true );
	}

	protected List<Way> vectorize( ImageryLayer layer, Point pos ) {
		if ( layer instanceof TMSLayer )
			return vectorizeTMS( (TMSLayer) layer, pos );

		return Collections.emptyList();
	}

	private List<Way> vectorizeTMS( TMSLayer layer, Point pos ) {
		AreaSelector areaSelector = new AreaSelector() {

			@Override
			public ColorSelector createColorSelector( BufferedImage img, int sx, int sy ) {
				return EllipsoidSelector.average( img, sx, sy, 2, 0 );
			}

		};

		TileArea area = areaSelector.select( layer, Main.map.mapView.getLatLon( pos.x - layer.getDx(), pos.y - layer.getDy() ) );

		//area.paint();

		return new BorderBuilder().build( area );
	}

	public void actionPerformed( ActionEvent e ) {
		if ( Main.map == null || Main.map.mapView == null || active )
			return;

		active = true;
		oldCursor = Main.map.mapView.getCursor();
		Main.map.mapView.setCursor( ImageProvider.getCursor( "crosshair", null ) );
		Main.map.mapView.addMouseListener( mouseListener );
	}

}
