package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class VectorizeAction extends JosmAction {

	private static final long serialVersionUID = 1L;

	private static final Shortcut shortcut = Shortcut.registerShortcut( "tools:vectorize", tr( "Tool: {0}", tr( "Vectorizer" ) ), KeyEvent.VK_V, Shortcut.CTRL_SHIFT );

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

				new Thread( new VectorizeTask( tr( "Vectorizing" ), imageryLayers.get( 0 ), e.getPoint() ) ).start();
			}
		}

	};
	private boolean active = false;
	private Cursor oldCursor = null;

	public VectorizeAction( String name ) {
		super( name, "lakewalker-sml", tr( "Vectorize feature." ), shortcut, true );

		setEnabled( true );
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
