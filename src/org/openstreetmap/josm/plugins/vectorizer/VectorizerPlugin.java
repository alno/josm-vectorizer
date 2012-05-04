package org.openstreetmap.josm.plugins.vectorizer;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class VectorizerPlugin extends Plugin {

	public VectorizerPlugin( PluginInformation info ) {
		super( info );
		
		MainMenu.add(Main.main.menu.toolsMenu, new VectorizeAction(tr("Vectorize feature")));
	}

}
