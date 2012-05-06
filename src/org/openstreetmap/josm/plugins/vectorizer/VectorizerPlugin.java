package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class VectorizerPlugin extends Plugin {

	private final VectorizeAction vectorizeAction = new VectorizeAction( tr( "Vectorize feature" ) );

	public VectorizerPlugin( PluginInformation info ) {
		super( info );

		MainMenu.add( Main.main.menu.toolsMenu, vectorizeAction );
	}

	@Override
	public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
		   vectorizeAction.setEnabled( newFrame != null );
	}

}
