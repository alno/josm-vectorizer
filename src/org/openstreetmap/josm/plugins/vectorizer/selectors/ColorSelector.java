package org.openstreetmap.josm.plugins.vectorizer.selectors;

public interface ColorSelector {

	boolean test( int color );

	ColorSelector scale( double scale );

}
